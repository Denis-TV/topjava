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
                if ( data.enabled == false ) {
                    $(row).attr("data-userEnabled", false);
                }
            }
        })
    );
});

function enable(id, checkedValue) {
    $.post(ctx.ajaxUrl + "enable/" + id,
    {
        value: checkedValue.valueOf()
    }
    ).done(function () {
        updateTable();
        successNoty("Saved");
    });
}