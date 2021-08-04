const userAjaxUrl = "admin/users/";

// https://stackoverflow.com/a/5064235/548473
const ctx = {
    ajaxUrl: userAjaxUrl
};

// $(document).ready(function () {
$(function () {
    makeEditable(
        $("#datatable").DataTable({
            "paging": false,
            "info": true,
            "columns": [
                {
                    "data": "name"
                },
                {
                    "data": "email"
                },
                {
                    "data": "roles"
                },
                {
                    "data": "enabled"
                },
                {
                    "data": "registered"
                },
                {
                    "defaultContent": "Edit",
                    "orderable": false
                },
                {
                    "defaultContent": "Delete",
                    "orderable": false
                }
            ],
            "order": [
                [
                    0,
                    "asc"
                ]
            ],
            "createdRow": function (row, data, index) {
                if (!data.enabled) {
                    $(row).attr("data-userEnabled", false);
                }
            }
        })
    );
});

function enable(id, checkedValue) {
    $.post(ctx.ajaxUrl + id,
    {
        enabled: checkedValue.valueOf()
    }
    ).done(function () {
        $("tr[id='" + id + "']").attr("data-userEnabled", checkedValue);
        successNoty(checkedValue === true ? "Enabled" : "Disabled");
    }).fail(function () {
        $("input[id='" + id+ "']").prop("checked", !checkedValue);
    });
}