<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bootstrap demo</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.6/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-4Q6Gf2aSP4eDXB8Miphtr37CMZZQ5oXLH2yaXMJ2w8e2ZtHTl7GptT4jmndRuHDT" crossorigin="anonymous">
  </head>
  <body>
   <!-- Bulk Upload Modal -->
    <div class="card">
      <div class=header">
        <h5 class="modal-title">Bulk Upload</h5>
        <button type="button" class="close" data-dismiss="modal">
          <span>&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label>Upload Type</label>
          <select class="form-control" id="uploadType">
            <option value="auctions">Auctions</option>
            <option value="collections">Collections</option>
          </select>
        </div>
        <div class="form-group">
          <label>CSV File</label>
          <input type="file" id="bulkUploadFile" class="form-control" accept=".csv">
          <small class="form-text text-muted">
            <a href="#" id="downloadTemplate">Download template CSV</a>
          </small>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
        <button type="button" class="btn btn-primary" id="uploadBtn">Upload</button>
      </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.6/dist/js/bootstrap.bundle.min.js" integrity="sha384-j1CDi7MgGQ12Z7Qab0qlWQ/Qqz24Gc6BM0thvEMVjHnfYGF0rmFCozFSxQBxwHKO" crossorigin="anonymous"></script>
<script src="https://code.jquery.com/jquery-3.7.1.min.js" integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>
<script>
$(document).ready(function() {
  // Download template
  $('#downloadTemplate').click(function(e) {
    e.preventDefault();
    const type = $('#uploadType').val();
    const headers = type === 'auctions' 
      ? ['scheme_id', 'auction_date', 'auction_amount', 'winner_id', 'amount', 'amount_paid', 'commission_earned', 'each_member_contribution', 'total_members']
      : ['scheme_id', 'customer_id', 'transaction_date', 'amount', 'payment_mode', 'remarks', 'collected_by'];
    
    let csvContent = headers.join(',') + '\n';
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `${type}_template.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  });
  
  // Handle upload
  $('#uploadBtn').click(function() {
    const file = $('#bulkUploadFile')[0].files[0];
    const type = $('#uploadType').val();
    
    if (!file) {
      alert('Please select a file');
      return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    formData.append('action', 'action-bulk-upload');
    formData.append('task', `upload-${type}`);
    formData.append('token', localStorage.getItem('authToken'));
    
    $.ajax({
      url: 'api.php',
      type: 'POST',
      data: formData,
      processData: false,
      contentType: false,
      success: function(response) {
        const res = JSON.parse(response);
        if (res.code === 200 || res.code === 207) {
          let msg = `Successfully processed ${res.success_count} records.`;
          if (res.error_count > 0) {
            msg += ` ${res.error_count} errors occurred.`;
            if (res.errors) {
              msg += '\n\nErrors:\n' + res.errors.join('\n');
            }
          }
          alert(msg);
          $('#bulkUploadModal').modal('hide');
          // Refresh the appropriate table
          if (type === 'auctions') {
            loadAuctions();
          } else {
            loadCollections();
          }
        } else {
          alert('Error: ' + res.message);
        }
      },
      error: function() {
        alert('An error occurred during upload');
      }
    });
  });
});
</script>  
</body>
</html>



