import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/masterannonce/api';

const loginDuration   = new Trend('login_duration', true);
const writeDuration   = new Trend('write_duration', true);
const authErrors      = new Rate('auth_errors');
const createdAnnonces = new Counter('annonces_created');

export const options = {
    scenarios: {
        // Lecture
        lecture_publique: {
            executor: 'ramping-vus',
            exec: 'scenarioLecture',
            stages: [
                { duration: '15s', target: 10 },
                { duration: '30s', target: 20 },
                { duration: '15s', target: 0  },
            ],
            gracefulRampDown: '5s',
        },
        // Écriture authentifiée
        ecriture_authentifiee: {
            executor: 'constant-vus',
            exec: 'scenarioEcriture',
            vus: 3,
            duration: '60s',
            startTime: '5s',
        },
    },
    thresholds: {
        http_req_failed:   ['rate<0.01'],
        http_req_duration: ['p(95)<800'],
        'http_req_duration{scenario:lecture_publique}':    ['p(95)<500'],
        'http_req_duration{scenario:ecriture_authentifiee}': ['p(95)<1200'],
        login_duration:    ['p(95)<600'],
        write_duration:    ['p(95)<1000'],
        auth_errors:       ['rate<0.05'],
    },
};

const JSON_HEADERS = { 'Content-Type': 'application/json' };

function authHeaders(token) {
    return { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` };
}

function login(username, password) {
    const res = http.post(
        `${BASE_URL}/login`,
        JSON.stringify({ username, password }),
        { headers: JSON_HEADERS, tags: { name: 'POST /login' } }
    );
    loginDuration.add(res.timings.duration);

    const ok = check(res, {
        'login status 200': (r) => r.status === 200,
        'login has token':  (r) => {
            try { return JSON.parse(r.body).token !== undefined; }
            catch (e) { return false; }
        },
    });

    authErrors.add(ok ? 0 : 1);
    if (!ok) return null;
    return JSON.parse(res.body).token;
}

// Scénario lecture publique
export function scenarioLecture() {
    group('lecture publique', () => {

        group('GET /annonces (paginé)', () => {
            const page = Math.floor(Math.random() * 3);
            const res = http.get(
                `${BASE_URL}/annonces?page=${page}&size=10`,
                { tags: { name: 'GET /annonces' } }
            );
            check(res, {
                'status 200':  (r) => r.status === 200,
                'a des items': (r) => {
                    try { return Array.isArray(JSON.parse(r.body).items); }
                    catch (e) { return false; }
                },
                'totalItems >= 0':  (r) => {
                    try { return JSON.parse(r.body).totalItems >= 0; }
                    catch (e) { return false; }
                },
            });
        });

        sleep(0.5);

        group('GET /categories', () => {
            const res = http.get(
                `${BASE_URL}/categories?page=0&size=20`,
                { tags: { name: 'GET /categories' } }
            );
            check(res, {
                'status 200':  (r) => r.status === 200,
                'a des items': (r) => {
                    try { return Array.isArray(JSON.parse(r.body).items); }
                    catch (e) { return false; }
                },
            });
        });

        sleep(0.5);

        group('GET /annonces/:id (détail)', () => {
            const list = http.get(
                `${BASE_URL}/annonces?page=0&size=5`,
                { tags: { name: 'GET /annonces (for detail)' } }
            );
            if (list.status === 200) {
                const items = JSON.parse(list.body).items;
                if (items && items.length > 0) {
                    const id = items[Math.floor(Math.random() * items.length)].id;
                    const res = http.get(
                        `${BASE_URL}/annonces/${id}`,
                        { tags: { name: 'GET /annonces/:id' } }
                    );
                    check(res, {
                        'détail status 200': (r) => r.status === 200,
                    });
                }
            }
        });

    });

    sleep(1);
}

// Scénario écriture authentifiée
export function scenarioEcriture() {
    const username = `loaduser_${__VU}`;
    const password = 'password123';

    group('écriture authentifiée', () => {

        // 1. Inscription (409 acceptable si déjà créé)
        group('POST /users (inscription)', () => {
            http.post(
                `${BASE_URL}/users`,
                JSON.stringify({
                    username: username,
                    email:    `${username}@loadtest.local`,
                    password: password,
                }),
                { headers: JSON_HEADERS, tags: { name: 'POST /users' } }
            );
        });

        sleep(0.3);

        // 2. Login
        const token = login(username, password);
        if (!token) return;

        sleep(0.3);

        // 3. Récupérer une catégorie existante
        const catRes = http.get(
            `${BASE_URL}/categories?page=0&size=5`,
            { tags: { name: 'GET /categories (for create)' } }
        );
        if (catRes.status !== 200) return;

        const categories = JSON.parse(catRes.body).items;
        if (!categories || categories.length === 0) return;

        const categoryId = categories[0].id;

        sleep(0.3);

        // 4. Créer une annonce
        group('POST /annonces', () => {
            const res = http.post(
                `${BASE_URL}/annonces`,
                JSON.stringify({
                    title:       `Annonce load VU${__VU} iter${__ITER}`,
                    description: `Description test de charge, itération ${__ITER}.`,
                    adress:      '10 rue du Test, 75001 Paris',
                    mail:        `${username}@loadtest.local`,
                    categoryId:  categoryId,
                }),
                { headers: authHeaders(token), tags: { name: 'POST /annonces' } }
            );

            writeDuration.add(res.timings.duration);

            const created = check(res, {
                'annonce créée 201': (r) => r.status === 201,
            });

            if (created) {
                createdAnnonces.add(1);

                const annonceId = JSON.parse(res.body).id;
                sleep(0.2);

                // 5. Publier l'annonce
                group('PUT /annonces/:id/publish', () => {
                    const pubRes = http.put(
                        `${BASE_URL}/annonces/${annonceId}/publish`,
                        null,
                        { headers: authHeaders(token), tags: { name: 'PUT /annonces/:id/publish' } }
                    );
                    check(pubRes, {
                        'publication status 200': (r) => r.status === 200,
                    });
                });
            }
        });

    });

    sleep(1);
}
