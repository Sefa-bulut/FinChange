import React, { useState } from 'react';
import { inviteUser } from '../../services/userService';

const styles = {
    modalBackdrop: { position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalContent: { background: 'white', padding: '30px', borderRadius: '8px', width: '450px', boxShadow: '0 5px 15px rgba(0,0,0,0.3)' },
    input: { width: '100%', padding: '10px', marginBottom: '15px', border: '1px solid #ccc', borderRadius: '4px' },
    select: { width: '100%', padding: '10px', marginBottom: '15px', border: '1px solid #ccc', borderRadius: '4px' },
    buttonContainer: { display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' },
    button: { padding: '10px 20px', border: 'none', borderRadius: '5px', cursor: 'pointer' },
    submitButton: { backgroundColor: '#c8102e', color: 'white' },
    cancelButton: { backgroundColor: '#f0f0f0', color: '#333' }
};

export default function InviteUserModal({ show, onClose }) {
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [personnelCode, setPersonnelCode] = useState('');
    const [roleNames, setRoleNames] = useState(['TRADER']);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [success, setSuccess] = useState('');

    if (!show) return null;

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError('');
        setSuccess('');
        try {
            const response = await inviteUser({ firstName, lastName, email, personnelCode, roleNames });
            setSuccess(response.message || 'Personel başarıyla davet edildi!');
            setFirstName(''); setLastName(''); setEmail(''); setPersonnelCode(''); setRoleNames(['TRADER']);
            setTimeout(() => {
                onClose();
            }, 2000);
        } catch (err) {
            setError(err.message || "Bir hata oluştu.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={styles.modalBackdrop} onClick={onClose}>
            <div style={styles.modalContent} onClick={e => e.stopPropagation()}>
                <h2>Yeni Personel Davet Et</h2>
                <form onSubmit={handleSubmit}>
                    <input style={styles.input} value={firstName} onChange={(e) => setFirstName(e.target.value)} placeholder="Ad" required />
                    <input style={styles.input} value={lastName} onChange={(e) => setLastName(e.target.value)} placeholder="Soyad" required />
                    <input style={styles.input} type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="E-Posta" required />
                    <input style={styles.input} value={personnelCode} onChange={(e) => setPersonnelCode(e.target.value)} placeholder="Personel Kodu" required />
                    <select style={styles.select} value={roleNames[0]} onChange={(e) => setRoleNames([e.target.value])}>
                        <option value="TRADER">Portföy Yöneticisi (Trader)</option>
                        <option value="ANALYST">Analist</option>
                        <option value="AUDITOR">Denetçi</option>
                        <option value="ADMIN">Yönetici</option>
                    </select>
                    {error && <p style={{ color: 'red' }}>{error}</p>}
                    {success && <p style={{ color: 'green' }}>{success}</p>}
                    <div style={styles.buttonContainer}>
                        <button type="button" style={{...styles.button, ...styles.cancelButton}} onClick={onClose}>İptal</button>
                        <button type="submit" style={{...styles.button, ...styles.submitButton}} disabled={isLoading}>
                            {isLoading ? 'Gönderiliyor...' : 'Davet Et'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}