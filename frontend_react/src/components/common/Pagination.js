import React from 'react';

const styles = {
    wrapper: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '20px 0 10px 0',
        userSelect: 'none',
    },
    button: {
        padding: '8px 16px',
        border: '1px solid #dee2e6',
        borderRadius: '5px',
        backgroundColor: '#fff',
        cursor: 'pointer',
        fontSize: '14px',
        fontWeight: '500',
        transition: 'background-color 0.2s, color 0.2s',
    },
    disabledButton: {
        backgroundColor: '#e9ecef',
        color: '#6c757d',
        cursor: 'not-allowed',
        borderColor: '#e9ecef',
    },
    pageInfo: {
        fontSize: '15px',
        color: '#495057',
        fontWeight: '500',
    }
};

const Pagination = ({ currentPage, totalPages, onPageChange }) => {
    if (totalPages <= 1) {
        return null;
    }

    const isFirstPage = currentPage === 0;
    const isLastPage = currentPage >= totalPages - 1;

    const handlePrevious = () => {
        if (!isFirstPage) {
            onPageChange(currentPage - 1);
        }
    };

    const handleNext = () => {
        if (!isLastPage) {
            onPageChange(currentPage + 1);
        }
    };

    return (
        <div style={styles.wrapper}>
            <button
                style={{
                    ...styles.button,
                    ...(isFirstPage ? styles.disabledButton : {})
                }}
                onClick={handlePrevious}
                disabled={isFirstPage}
            >
                &larr; Önceki
            </button>

            <div style={styles.pageInfo}>
                Sayfa {currentPage + 1} / {totalPages}
            </div>

            <button
                style={{
                    ...styles.button,
                    ...(isLastPage ? styles.disabledButton : {})
                }}
                onClick={handleNext}
                disabled={isLastPage}
            >
                Sonraki &rarr;
            </button>
        </div>
    );
};

export default Pagination;