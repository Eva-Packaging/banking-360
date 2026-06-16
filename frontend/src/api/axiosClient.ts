import axios from 'axios';

const axiosClient = axios.create({
  baseURL: 'http://localhost:8080/api', // API Gateway — all frontend calls go here
});

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token'); // JWT saved after login
  if (token) {
    config.headers.Authorization = `Bearer ${token}`; // attach JWT to secured requests
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token'); // clear bad/expired token
      window.location.href = '/login'; // send user back to login
    }
    return Promise.reject(error);
  }
);

export default axiosClient;