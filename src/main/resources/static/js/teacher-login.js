function onClickLogin() {
  let username = $('#user').val();
  let password = $('#pass').val();
  $.ajax("/api/teacherLogin", {
    method: 'POST',
    data: {
      username: username,
      password: password
    },
    success: function (data, status, xhr) {
      Cookies.set('mashovId', data.user.mashovId).set('token', data.user.token);
      Cookies.set('teacherCsrf', data.csrfToken);
      Cookies.set('teacherCookies', data.cookies);
      window.location.href = "/teacher";
    },
    error: function (jqXhr, textStatus, errorMessage) {
      alert("Invalid login. Please try again.");
    }
  });
}

document.getElementById("login").onclick = onClickLogin;