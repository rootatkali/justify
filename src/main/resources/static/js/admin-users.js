const tb = document.getElementById("tb");

let users;

$.get("/api/admin/users", data => {
  users = data;
  tb.innerHTML = "";
  users.forEach(u => {
    let id = u.mashovId;
    let first = u.firstName;
    let last = u.lastName;
    let role = u.role;
    let action = `
<button class="btn btn-sm btn-info" type="button" data-bs-toggle="modal" data-bs-target="modal" data-bs-id="${id}">
  <i class="fal fa-edit" style="color: #fff"></i>
</button>
`;
    tb.innerHTML += `<tr><td>${id}</td><td>${first}</td><td>${last}</td><td>${role}</td><td>${action}</td></tr>`
  });
});


const modal = document.getElementById("modal");
modal.addEventListener("show.bs.modal", event => {
  let btn = event.relatedTarget;
  let id = btn.getAttribute('data-bs-id');
  $("#modalTitle").text(`Editing user ${id}`);
  $.get(`/api/admin/users/${id}`, u => {
    $("#first").val(u.firstName);
    $("#last").val(u.lastName);
    document.getElementById("submit").onclick = () => {
      $.ajax(`/api/admin/users/${id}/name`, {
        method: "POST",
        data: {
          first: $("#first").val(),
          last: $("#last").val()
        },
        success: data => location.reload()
      });
    }
  })
});