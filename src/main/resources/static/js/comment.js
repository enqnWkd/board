// static/js/comment.js

document.addEventListener('DOMContentLoaded', function () {
    const submitBtn = document.getElementById('submit-comment');
    if (submitBtn) {
        submitBtn.addEventListener('click', function () {
            const content = document.getElementById('comment-content').value;

            fetch('/api/comments', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    articleId: articleId,
                    content: content
                })
            })
                .then(response => {
                    if (response.ok) {
                        alert('댓글이 등록되었습니다!');
                        window.location.reload();
                    } else {
                        alert('댓글 등록에 실패했습니다.');
                    }
                })
                .catch(error => {
                    console.error(error);
                    alert('오류가 발생했습니다.');
                });
        });
    }
});

document.addEventListener('DOMContentLoaded', () => {
    const deleteButtons = document.querySelectorAll('.delete-comment-btn');

    deleteButtons.forEach(button => {
        button.addEventListener('click', () => {
            const commentId = button.getAttribute('data-comment-id');
            if (!confirm('정말 삭제하시겠습니까?')) return;

            fetch(`/api/articles/${articleId}/comments/${commentId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('댓글 삭제 권한이 없거나 오류 발생');
                    }
                    location.reload();
                })
                .catch(error => alert(error.message));
        });
    });
});