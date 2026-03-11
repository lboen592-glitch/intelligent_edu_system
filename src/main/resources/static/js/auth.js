(function () {
    const userId = localStorage.getItem("userId");
    if (!userId || userId === "undefined" || userId === "null") {
        location.href = "login.html";
        return;
    }
    const api = axios.create({
        baseURL: "http://localhost:8080",
        timeout: 10000
    });
    api.interceptors.request.use((config) => {
        const uid = localStorage.getItem("userId");
        if (uid) config.headers["X-User-Id"] = uid;
        return config;
    });

    api.interceptors.response.use(
        (res) => res,
        (err) => {
            if (err.response && err.response.status === 401) {
                localStorage.removeItem("userId");
                localStorage.removeItem("username");
                location.href = "login.html";
            }
            return Promise.reject(err);
        }
    );
    window.api = api;
})();