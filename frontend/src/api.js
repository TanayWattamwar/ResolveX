import axios from 'axios';
const api=axios.create({baseURL:import.meta.env.VITE_API_URL||'http://localhost:8080/api'});
api.interceptors.request.use(config=>{const token=localStorage.getItem('rx_token');if(token)config.headers.Authorization=`Bearer ${token}`;return config;});
api.interceptors.response.use(r=>r,e=>{if(e.response?.status===401){localStorage.removeItem('rx_token');localStorage.removeItem('rx_user');if(!location.pathname.includes('/login'))location.href='/login';}return Promise.reject(e);});
export default api;
