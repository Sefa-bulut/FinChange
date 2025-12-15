import React from 'react';

const styles = {
    overlay: {
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.6)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
    },
    modal: {
        backgroundColor: '#fff',
        padding: '25px 30px',
        borderRadius: '10px',
        boxShadow: '0 5px 15px rgba(0, 0, 0, 0.3)',
        width: '90%',
        maxWidth: '500px',
        position: 'relative',
    },
    header: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        borderBottom: '1px solid #e9ecef',
        paddingBottom: '15px',
        marginBottom: '20px',
    },
    title: {
        margin: 0,
        fontSize: '22px',
        color: '#343a40',
    },
    closeButton: {
        background: 'transparent',
        border: 'none',
        fontSize: '28px',
        lineHeight: '1',
        cursor: 'pointer',
        padding: '0 5px',
        color: '#6c757d',
    },
    body: {
        fontSize: '16px',
        color: '#495057',
        lineHeight: '1.6',
    },
    footer: {
        display: 'flex',
        justifyContent: 'flex-end',
        gap: '10px',
        marginTop: '30px',
    },
    button: {
        padding: '10px 20px',
        borderRadius: '5px',
        border: 'none',
        fontSize: '15px',
        cursor: 'pointer',
        fontWeight: '500',
    },
    cancelButton: {
        backgroundColor: '#6c757d',
        color: 'white',
    },
    confirmButton: {
        backgroundColor: '#c8102e',
        color: 'white',
    }
};

const ConfirmationModal = ({ isOpen, onClose, onConfirm, title, children }) => {
    if (!isOpen) {
        return null;
    }

    return (
        <div style={styles.overlay} onClick={onClose}>
            <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
                <div style={styles.header}>
                    <h2 style={styles.title}>{title}</h2>
                    <button style={styles.closeButton} onClick={onClose}>
                        &times; {/* Bu 'x' karakterini oluşturur */}
                    </button>
                </div>
                <div style={styles.body}>
                    {children} {/* Modal'ın içeriği buraya gelir */}
                </div>
                <div style={styles.footer}>
                    <button
                        style={{ ...styles.button, ...styles.cancelButton }}
                        onClick={onClose}
                    >
                        İptal
                    </button>
                    <button
                        style={{ ...styles.button, ...styles.confirmButton }}
                        onClick={onConfirm}
                    >
                        Onayla
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmationModal;