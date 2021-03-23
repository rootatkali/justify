const mashovId = Cookies.get("mashovId");
const tb = document.getElementById("tb");

let current;
let events;
let justifications;
let users;


$.get("/api/events", evn => {
  events = evn;
  $.get("/api/justifications", jst => {
    justifications = jst;
    $.get("/api/users", usr => {
      users = usr;
      $.get("/api/requests/unanswered", requests => {
        tb.innerHTML = "";
        current = requests.reverse();
        display();
      });
    });
  });
});

function approveRequest(requestId) {
  $.get(`/api/requests/${requestId}/approve`, (data) => {
    location.reload();
  });
}

function rejectRequest(requestId) {
  $.get(`/api/requests/${requestId}/reject`, (data) => {
    location.reload();
  });
}

$.get(`/api/users/${mashovId}`, data => {
  $("#name").text(data.firstName + " " + data.lastName);
});

document.getElementById("btnLogout").onclick = function () {
  $.get("/api/logout", () => {
    window.location.href = "/teacher/login";
  });
}

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
    let approval = justifications.filter (j => j.code === r.justificationCode)[0].name;
    let note = r.note;
    let status = r.status;
    let color = "";
    if (r.status === "UNANSWERED") {
      status = `
<div class="btn-group btn-group-sm" role="group" aria-label="Approve/reject buttons">
  <button class="btn btn-success" onclick="approveRequest(${id})" aria-label="Approve">
    <i class="fal fa-check-square" style="color:#fff;"></i>
  </button>
  <button class="btn btn-danger" onclick="rejectRequest(${id})" aria-label="Reject">
    <i class="fal fa-times-square" style="color:#fff;"></i>
  </button>
</div>`
    } else {
      switch (r.status) {
        case "APPROVED":
          color = 'class="table-success"'; // green
          status = status +
            ` (<a href="#" onclick="undo(${id})"><i class="fal fa-trash-undo" style="color: red"></i></a>)`;
          break;
        case "REJECTED":
          color = 'class="table-danger"'; // red
          status = status +
            ` (<a href="#" onclick="unlock(${id})"><i class="fal fa-trash-restore" style="color: black"></i></a>)`;
          break;
        case "CANCELLED":
          color = 'class="table-secondary"'; // gray
          break;
        default: // UNANSWERED
          color = ''; // default (white/black - browser settings)
      }
    }
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
<td>${status}</td>
</tr>`;
    tb.innerHTML += tr;
  });
}

function loadAll() {
  $.get("/api/requests", requests => {
    current = requests.reverse();
    display();
  });
}

function loadUnanswered() {
  $.get("/api/requests/unanswered", requests => {
    current = requests.reverse();
    display();
  });
}

function sortUser() {
  current = current.sort((a, b) => {
    let userA = users.filter(u => u.mashovId === a.mashovId)[0];
    let userB = users.filter(u => u.mashovId === b.mashovId)[0];
    return userA.firstName.localeCompare(userB.firstName);
  });
  display();
}

function sortDate() {
  current = current.sort((a, b) => {
    return a.requestId - b.requestId;
  }).reverse();
  display();
}

function unlock(id) {
  $.get(`/api/requests/${id}/unlock`, data => {
    window.location.reload();
  });
}

function undo(id) {
  $.get(`/api/requests/${id}/undo`, data => {
    window.location.reload();
  })
}