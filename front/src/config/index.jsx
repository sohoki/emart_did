const raw = import.meta.env.VITE_REACT_APP_API_URL;
const raw_image = import.meta.env.VITE_REACT_APP_IMG_URL;
const config = {
  REACT_APP_API_URL: raw
    ? (raw.startsWith('http') ? raw : "http://"+raw)
    : 'http://27.96.130.69:7001',
  REACT_APP_IMG_URL: raw 
    ? (raw.startsWith('http') ? raw+raw_image : "http://"+raw+raw_image)
    : 'http://27.96.130.69:7001/upload/',
};

export default config;
