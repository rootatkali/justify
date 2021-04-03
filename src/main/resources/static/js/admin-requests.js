const tb = document.getElementById("tb");

let current;
let events;
let justifications;
let users;


$.get("/api/events", evn => {
  events = evn;
  $.get("/api/justifications", jst => {
    justifications = jst;
    $.get("/api/admin/users", usr => {
      users = usr;
      $.get("/api/admin/requests", requests => {
        tb.innerHTML = "";
        current = requests.reverse();
        display();
      });
    });
  });
});

function display() {
  tb.innerHTML = "";
  current.forEach(r => {
    let id = r.requestId;
    let user = users.filter(u => u.mashovId === r.mashovId)[0];
    let name = user.firstName + " " + user.lastName;
    let sDate = r.dateStart;
    let sPeriod = r.periodStart;
    let eDate = r.dateEnd;
    let ePeriod = r.periodEnd;
    let event = events.filter(e => e.code === r.eventCode)[0].name;
    let approval = justifications.filter(j => j.code === r.justificationCode)[0].name;
    let note = r.note;
    let color = "";
    switch (r.status) {
      case "APPROVED":
        color = 'class="table-success"'; // green
        break;
      case "REJECTED":
        color = 'class="table-danger"'; // red
        break;
      case "CANCELLED":
        color = 'class="table-secondary"'; // gray
        break;
    }

    let actions = `
    <div class="btn-group btn-group-sm" role="group" aria-label="Action buttons">
      <button type="button" class="btn btn-success" onclick="approve(${id})"><i class="fal fa-check-square" style="color:#fff;"></i></button>
      <button type="button" class="btn btn-danger" onclick="reject(${id})"><i class="fal fa-check-times" style="color:#fff;"></i></button>
      <button type="button" class="btn btn-secondary" onclick="cancel(${id})"><i class="fal fa-ban" style="color:#fff;"></i></button>
      <button type="button" class="btn btn-primary" onclick="reset(${id})"><i class="fal fa-undo" style="color:#fff;"></i></button>
      <button type="button" class="btn btn-info" data-bs-toggle="modal" data-bs-target="#editModal" data-bs-id="${id}">
        <i class="fal fa-edit" style="color: #fff"></i>
      </button>
      <button type="button" class="btn btn-outline-danger" onclick="del(${id})"><i class="fal fa-trash" style="color:red;"></i></button>
    </div>
    `;

    let tr = `<tr ${color}>
<td>${id}</td>
<td>${name}</td>
<td>${sDate}</td>
<td>${sPeriod}</td>
<td>${eDate}</td>
<td>${ePeriod}</td>
<td>${event}</td>
<td>${approval}</td>
<td>${note}</td>
<td>${actions}</td>
</tr>`;
    tb.innerHTML += tr;
  });
}

function approve(id) {
  $.ajax(`/api/admin/requests/${id}/approve`, {
    method: "POST",
    success: data => location.reload()
  });
}

function reject(id) {
  $.ajax(`/api/admin/requests/${id}/reject`, {
    method: "POST",
    success: data => location.reload()
  });
}

function cancel(id) {
  $.ajax(`/api/admin/requests/${id}/cancel`, {
    method: "POST",
    success: data => location.reload()
  });
}

function reset(id) {
  $.ajax(`/api/admin/requests/${id}/reset`, {
    method: "POST",
    success: data => location.reload()
  });
}

function del(id) {
  $.ajax(`/api/admin/requests/${id}`, {
    method: "DELETE",
    success: data => location.reload()
  });
}

function sortName() {
  current = current.sort((a, b) => {
    let userA = users.filter(u => u.mashovId === a.mashovId)[0];
    let userB = users.filter(u => u.mashovId === b.mashovId)[0];
    return userA.firstName.localeCompare(userB.firstName);
  });
  display();
}

function sortId() {
  current = current.sort((a, b) => {
    return a.requestId - b.requestId;
  }).reverse();
  display();
}

const editModal = document.getElementById('editModal');
editModal.addEventListener('show.bs.modal', event => {
  const btn = event.relatedTarget;
  const id = btn.getAttribute('data-bs-id');
  $.get(`/api/admin/requests/${id}`, data => {
    $('#editModalTitle').text(`Editing request ${id}`);
    document.getElementById('edit-date-start').value = data.dateStart;
    document.getElementById('edit-date-end').value = data.dateEnd;
    document.getElementById('edit-period-start').value = data.periodStart;
    document.getElementById('edit-period-end').value = data.periodEnd;
    document.getElementById('edit-approval').value = data.justificationCode;
    document.getElementById('edit-event').value = data.eventCode;
    document.getElementById('edit-note').value = data.note;
    document.getElementById('edit-sendRequest').onclick = () => {
      $.ajax(`/api/admin/requests/${id}`, {
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify({
          mashovId: data.mashovId,
          dateStart: $('#edit-date-start').val(),
          dateEnd: $('#edit-date-end').val(),
          periodStart: parseInt($('#edit-period-start').val()),
          periodEnd: parseInt($('#edit-period-end').val()),
          eventCode: parseInt($('#edit-event').val()),
          justificationCode: parseInt($('#edit-approval').val()),
          note: $('#edit-note').val()
        }),
        success: successData => {
          location.reload();
        },
        error: (jqXHR, textStatus, errorThrown) => {
          alert(errorThrown)
        }
      });
    };
  });
});