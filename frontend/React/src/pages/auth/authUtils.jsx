export function getToken() {
    return sessionStorage.getItem("token");
}

export function authHeaders(extra = {}) {
    return {
        Authorization: `Bearer ${getToken()}`,
        ...extra
    };
}
