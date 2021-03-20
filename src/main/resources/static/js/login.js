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
      window.location.href = "/student";
    },
    error: function (jqXhr, textStatus, errorMessage) {
      alert("Invalid login. Please try again.");
      // TODO error processing logic
    }
  });
}

document.getElementById("login").onclick = onClickLogin;