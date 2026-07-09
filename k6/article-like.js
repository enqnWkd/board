import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 200 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.01'],
    },
};

export function setup() {
    const loginRes = http.post(
        'http://15.134.211.88:8080/auth/login',
        JSON.stringify({ email: 'testuser1@test.com', password: '1234' }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    console.log("status:", loginRes.status);
    console.log("body:", loginRes.body);

    const token = loginRes.json('accessToken');
    return { token };
}

export default function (data) {
    const res = http.post('http://15.134.211.88:8080/api/articles/1/likes', null, {
        headers: { Authorization: `Bearer ${data.token}` },
    });

    check(res, {
        'like status is 200': (r) => r.status === 200,
    });

    sleep(0.1);
}