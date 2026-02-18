import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/masterannonce/api';

export const options = {
    stages: [
        { duration: '10s', target: 5 },
        { duration: '20s', target: 10 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

export default function () {
    // GET /api/annonces (liste paginee)
    let resAnnonces = http.get(`${BASE_URL}/annonces?page=0&size=10`);
    check(resAnnonces, {
        'GET /annonces status 200': (r) => r.status === 200,
        'GET /annonces has items': (r) => JSON.parse(r.body).items !== undefined,
    });

    // GET /api/categories (liste paginee)
    let resCategories = http.get(`${BASE_URL}/categories?page=0&size=10`);
    check(resCategories, {
        'GET /categories status 200': (r) => r.status === 200,
        'GET /categories has items': (r) => JSON.parse(r.body).items !== undefined,
    });

    // GET /api/helloWorld
    let resHello = http.get(`${BASE_URL}/helloWorld`);
    check(resHello, {
        'GET /helloWorld status 200': (r) => r.status === 200,
    });

    sleep(1);
}
