import http from 'k6/http';
import { check, fail } from 'k6';

const baseUrl = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const token = __ENV.VITRAL_TOKEN;

const rate = Number(__ENV.RATE || 5);
const duration = __ENV.DURATION || '30s';
const preAllocatedVUs = Number(__ENV.PRE_ALLOCATED_VUS || 10);
const maxVUs = Number(__ENV.MAX_VUS || 50);

export const options = {
  scenarios: {
    home: scenario('home'),
    busca: scenario('busca'),
    recomendacoes: scenario('recomendacoes'),
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{endpoint:home}': ['p(95)<750'],
    'http_req_duration{endpoint:busca}': ['p(95)<900'],
    'http_req_duration{endpoint:recomendacoes}': ['p(95)<1000'],
    dropped_iterations: ['count==0'],
  },
};

function scenario(exec) {
  return {
    executor: 'constant-arrival-rate',
    exec,
    rate,
    timeUnit: '1s',
    duration,
    preAllocatedVUs,
    maxVUs,
  };
}

export function setup() {
  if (!token) {
    fail('Defina VITRAL_TOKEN com um token de usuário para testar recomendações.');
  }
}

export function home() {
  const response = http.get(`${baseUrl}/api/v1/home`, { tags: { endpoint: 'home' } });
  check(response, { 'Home responde 200': (result) => result.status === 200 });
}

export function busca() {
  const response = http.get(
    `${baseUrl}/api/v1/busca/produtos?q=livro&page=0&size=20`,
    { tags: { endpoint: 'busca' } },
  );
  check(response, { 'Busca responde 200': (result) => result.status === 200 });
}

export function recomendacoes() {
  const response = http.get(`${baseUrl}/api/v1/recomendacoes?page=0&size=20`, {
    headers: { Authorization: `Bearer ${token}` },
    tags: { endpoint: 'recomendacoes' },
  });
  check(response, { 'Recomendações respondem 200': (result) => result.status === 200 });
}
