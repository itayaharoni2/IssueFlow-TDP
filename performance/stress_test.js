import http from 'k6/http';
import { check, sleep } from 'k6';

// k6 options to define the load profile
export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Ramp-up to 20 virtual users (VUs)
    { duration: '1m', target: 50 },   // Stress phase: ramp-up to 50 VUs
    { duration: '1m', target: 100 },  // Heavy stress: ramp-up to 100 VUs
    { duration: '30s', target: 0 },   // Cool-down to 0 VUs
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete under 500ms
    http_req_failed: ['rate<0.01'],   // Request failure rate must be less than 1%
  },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
  // 1. Authenticate to get a JWT token
  const loginPayload = JSON.stringify({
    username: 'jdoe',
    password: 'secret',
  });

  const loginParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, loginParams);

  const loginCheck = check(loginRes, {
    'login status is 200': (r) => r.status === 200,
    'has access token': (r) => {
      try {
        return JSON.parse(r.body).accessToken !== undefined;
      } catch (e) {
        return false;
      }
    },
  });

  if (!loginCheck) {
    // If login failed, sleep and skip next steps
    sleep(1);
    return;
  }

  const token = JSON.parse(loginRes.body).accessToken;

  const authParams = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  };

  // 2. Fetch authenticated user profile
  const meRes = http.get(`${BASE_URL}/auth/me`, authParams);
  check(meRes, {
    'get me status is 200': (r) => r.status === 200,
  });

  // 3. Fetch tickets for a project (e.g. project 1)
  const ticketsRes = http.get(`${BASE_URL}/tickets/project/1`, authParams);
  check(ticketsRes, {
    'get tickets status is 200 or 404': (r) => r.status === 200 || r.status === 404,
  });

  // Pacing: wait 1 second between iterations per virtual user
  sleep(1);
}
