import axios from 'axios';

const PORT = 8081; // Spring Boot Backend Port
const BASE_URL = `http://localhost:${PORT}`;

export const apiClient = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request Interceptor: Attach the bearer token automatically
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
            console.debug('[API] Token attached:', token.substring(0, 20) + '...');
        } else {
            console.warn('[API] No token in localStorage for request:', config.url);
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response Interceptor: Catch 401 Unauthorized globally and redirect
apiClient.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        console.error('[API] Error:', error.config?.url, error.response?.status, error.response?.data);
        if (error.response && error.response.status === 401) {
            // Clear local storage and redirect to login
            localStorage.removeItem('token');
            const publicPaths = ['/login', '/register', '/'];
            if (!publicPaths.includes(window.location.pathname)) {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);
export default apiClient;
