import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '1m', target: 200 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],  // 95%가 2초 이내
        http_req_failed: ['rate<0.01'],     // 실패율 1% 미만
    },
};

export function setup() {
    const loginRes = http.post(
        'http://3.27.88.6:8080/auth/login',
        JSON.stringify({
         email: 'testuser1@test.com', password: '1234'
         }),
        {
            headers: { 'Content-Type': 'application/json'
            }
        }
    );
    check(loginRes, {
        'login success': (r) => r.status === 200,
    });

    const token = loginRes.json('accessToken');
    console.log(`token: ${token}`);

    return { token };
}

export default function (data) {
    const res = http.get('http://3.27.88.6:8080/api/articles/1', {
        headers: { Authorization: `Bearer ${data.token}` },
    });

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.1);
}