const mealsAjaxUrl = "user/meals/";
let filterForm;

const ctx = {
    ajaxUrl: mealsAjaxUrl
};

$(function () {
    makeEditable(
        $("#datatable").DataTable({
            "paging": false,
            "info": true,
            "columns": [
                {
                    "data": "dateTime"
                },
                {
                    "data": "description"
                },
                {
                    "data": "calories"
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
                    "desc"
                ]
            ]
        })
    );
    filterForm = $("#filterForm");
});

function filter() {
    $.get(ctx.ajaxUrl + "filtered",
        filterForm.serialize(),
        function (data) {
            refillDatatable(data)
        });
}

function filterCancel() {
    filterForm[0].reset();
    updateTable();
}