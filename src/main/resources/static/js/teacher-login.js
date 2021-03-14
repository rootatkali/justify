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
      Cookies.set('mashovId', data.mashovId).set('token', data.token);
      window.location.href = "/teacher";
    },
    error: function (jqXhr, textStatus, errorMessage) {
      alert('error!');
    }
  });
}

document.getElementById("login").onclick = onClickLogin;