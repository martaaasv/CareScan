document.addEventListener("DOMContentLoaded", function () {

    function openLogoutModal() {
        document.getElementById("logoutModal").classList.add("active");
    }

    function closeLogoutModal() {
        document.getElementById("logoutModal").classList.remove("active");
    }

    document.getElementById("logoutModal").addEventListener("click", function (e) {
        if (e.target === this) closeLogoutModal();
    });

    window.openLogoutModal = openLogoutModal;
    window.closeLogoutModal = closeLogoutModal;
});