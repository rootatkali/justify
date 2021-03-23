let events;
let justifications;

$.get("/api/events", evn => {
  events = evn;
  loadEvents();
  $.get("/api/justifications", jst => {
    justifications = jst;
    loadApprovals();
    getRequests();
  });
});

function loadRequests(requests) {
  let tb = document.getElementById('tb');
  tb.innerHTML = "";
  let event = 0;

  requests.reverse().forEach(r => {
    let color = '';
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
      default: // UNANSWERED
        color = ''; // default (white/black - browser settings)
    }
    let eventCode = r.eventCode;
    let justificationCode = r.justificationCode;
    let eventName = events.filter(e => e.code === eventCode)[0].name;
    let jstfnName = justifications.filter(j => j.code === justificationCode)[0].name;
    let status = r.status === "UNANSWERED" ?
      `
<div class="btn-group btn-group-sm" role="group" aria-label="Action buttons for request">
  <button type="button" class="btn btn-info" data-bs-toggle="modal" data-bs-target="#editModal" data-bs-id="${r.requestId}">
    <i class="fal fa-edit" style="color: #fff"></i>
  </button>
  <button type="button" class="btn btn-danger" onclick="cancelRequest(${r.requestId})">
    <i class="fal fa-trash-alt" style="color: #fff"></i>
  </button>
</div>
`
      : r.status;
    let tr = `<tr ${color}>
<td>${r.requestId}</td>
<td>${r.dateStart}</td>
<td>${r.periodStart}</td>
<td>${r.dateEnd}</td>
<td>${r.periodEnd}</td>
<td>${eventName}</td>
<td>${jstfnName}</td>
<td>${r.note}</td>
<td>${status}</td>
</tr>`
    tb.innerHTML += tr;
  });
}

function getRequests() {
  let mashovId = Cookies.get("mashovId");
  let url = `/api/users/${mashovId}/requests`;

  $.get(url, loadRequests);
}

function cancelRequest(id) {
  $.get(`/api/requests/${id}/cancel`, data => {
    window.location.reload();
  })
}

function loadEvents() {
  let select = document.getElementById("event");
  select.innerHTML = "";
  events.forEach(e => {
    select.innerHTML += `<option value="${e.code}">${e.name}</option>`;
  });
  document.getElementById("edit-event").innerHTML = select.innerHTML;
}

function loadApprovals() {
  let select = document.getElementById("approval");
  select.innerHTML = "";
  justifications.forEach(j => {
    select.innerHTML += `<option value="${j.code}">${j.name}</option>`;
  });
  document.getElementById("edit-approval").innerHTML = select.innerHTML;
}

function sendRequest() {
  let mashovId = Cookies.get("mashovId");
  let dateStart = $('#date-start').val();
  let dateEnd = $('#date-end').val();
  if (dateEnd == null || dateEnd === '') {
    dateEnd = dateStart;
  }
  let periodStart = parseInt($('#period-start').val());
  let periodEnd = parseInt($('#period-end').val());
  let eventCode = parseInt($('#event').val());
  let justificationCode = parseInt($('#approval').val());
  let note = $('#note').val();

  $.ajax("/api/requests", {
    method: 'POST',
    contentType: 'application/json',
    data: JSON.stringify({
      mashovId: mashovId,
      dateStart: dateStart,
      dateEnd: dateEnd,
      periodStart: periodStart,
      periodEnd: periodEnd,
      eventCode: eventCode,
      justificationCode: justificationCode,
      note: note
    }),
    success: data => {
      new bootstrap.Modal(document.getElementById('modal')).hide();
      location.reload();
    },
    error: (jqXHR, textStatus, errorThrown) => alert(errorThrown)
  });
}

document.getElementById("sendRequest").onclick = function () {
  sendRequest()
};

document.getElementById("btnLogout").onclick = function () {
  $.get("/api/logout", () => {
    window.location.href = "/student/login";
  });
}

let mashovId = Cookies.get("mashovId");
$.get(`/api/users/${mashovId}`, data => {
  $("#name").text(data.firstName + " " + data.lastName);
});

const editModal = document.getElementById('editModal');
editModal.addEventListener('show.bs.modal', event => {
  const btn = event.relatedTarget;
  const id = btn.getAttribute('data-bs-id');
  $.get(`/api/requests/${id}`, data => {
    $('#editModalTitle').text(`Editing request ${id}`);
    document.getElementById('edit-date-start').value = data.dateStart;
    document.getElementById('edit-period-start').value = data.periodStart;
    document.getElementById('edit-date-end').value = data.dateEnd;
    document.getElementById('edit-period-end').value = data.periodEnd;
    document.getElementById('edit-event').value = data.eventCode;
    document.getElementById('edit-approval').value = data.justificationCode;
    document.getElementById('edit-note').value = data.note;
    document.getElementById('edit-sendRequest').onclick = () => {
      $.ajax(`/api/requests/${id}`, {
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