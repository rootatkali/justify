function onClickLogin() {
  let username = $('#user').val();
  let password = $('#pass').val();
  $.ajax("/api/login", {
    method: 'POST',
    data: {
      username: username,
      password: password
    },
    success: function (data, status, xhr) {
      Cookies.set('mashovId', data.mashovId).set('token', data.token);
      window.location.href = "/student";
    },
    error: function (jqXhr, textStatus, errorMessage) {
      alert('error!');
      // TODO error processing logic
    }
  });
}

document.getElementById("login").onclick = onClickLogin;