// js/auth.js
(function () {
    const token = localStorage.getItem("token");
    if (!token || token === "undefined" || token === "null") {
        location.href = "login.html";
        return;
    }

    const api = axios.create({
        baseURL: "http://localhost:8080",
        timeout: 10000
    });

    api.interceptors.request.use((config) => {
        const t = localStorage.getItem("token");
        if (t) config.headers.Authorization = "Bearer " + t;
        return config;
    });

    api.interceptors.response.use(
        (res) => res,
        (err) => {
            if (err.response && err.response.status === 401) {
                localStorage.removeItem("token");
                localStorage.removeItem("username");
                location.href = "login.html";
            }
            return Promise.reject(err);
        }
    );

    // 挂到全局，页面里直接用 api
    window.api = api;
})();
