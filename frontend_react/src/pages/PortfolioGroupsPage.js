import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom'; 
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup'; 
import { createGroupSchema } from '../validation/portfolioGroupSchemas';
import * as groupService from '../services/portfolioGroupService';
import { getAllClients } from '../services/customerService';
import { toast } from 'react-toastify';

const styles = {
    page: { padding: '30px', display: 'flex', gap: '30px', height: 'calc(100vh - 120px)' },
    header: { fontSize: '28px', fontWeight: 'bold', color: '#333', marginBottom: '20px' },
    panel: { backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', flex: 1, display: 'flex', flexDirection: 'column' },
    list: { listStyle: 'none', padding: 0, margin: 0, overflowY: 'auto', flexGrow: 1 },
    listItem: { padding: '12px 15px', borderBottom: '1px solid #eee', cursor: 'pointer' },
    activeListItem: { backgroundColor: '#eef2ff' },
    formGroup: { marginBottom: '20px', display: 'flex', alignItems: 'flex-start', gap: '10px' },
    input: {  padding: '10px', border: '1px solid #ccc', borderRadius: '4px' },
    button: { padding: '10px 20px', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' },
    submitButton: { backgroundColor: '#c8102e', color: 'white' },
    removeButton: { backgroundColor: '#fbebeb', color: '#c8102e', marginLeft: 'auto' },
    customerListItem: { display: 'flex', alignItems: 'center', padding: '8px 10px', borderBottom: '1px solid #eee' },
    checkbox: { marginRight: '10px' },
    errorMessage: { color: 'red', marginTop: '5px', fontSize: '14px' } 
};

export default function PortfolioGroupsPage() {
    const [groups, setGroups] = useState([]);
    const [selectedGroup, setSelectedGroup] = useState(null);
    const [members, setMembers] = useState([]);
    const [allClients, setAllClients] = useState([]);
    const [selectedClients, setSelectedClients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState(''); 
    const navigate = useNavigate(); 

    const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
        resolver: yupResolver(createGroupSchema)
    });

    const fetchData = useCallback(async () => {
        setLoading(true);
        try {
            const [groupsData, clientsData] = await Promise.all([
                groupService.getMyGroups(),
                getAllClients()
            ]);
            setGroups(groupsData);
            setAllClients(clientsData);
        } catch (error) {
            alert('Veriler yüklenirken bir hata oluştu.');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    useEffect(() => {
        if (selectedGroup) {
            const fetchMembers = async () => {
                try {
                    const membersData = await groupService.getActiveMembers(selectedGroup.id);
                    setMembers(membersData);
                } catch (error) {
                    alert('Grup üyeleri yüklenirken bir hata oluştu.');
                }
            };
            fetchMembers();
        } else {
            setMembers([]);
        }
    }, [selectedGroup]);

    const handleCreateGroup = async (data) => {
        try {
            const created = await groupService.createGroup(data.groupName);
            reset({ groupName: '' }); // Formu temizle
            // Grupları yenile ve yeni oluşturulan grubu seç
            const groupsData = await groupService.getMyGroups();
            setGroups(groupsData);
            const justCreated = created ? groupsData.find(g => g.id === created.id) : null;
            if (justCreated) setSelectedGroup(justCreated);
            toast.success('Grup oluşturuldu');
        } catch (error) {
            toast.error(error.message || 'Grup oluşturulamadı.');
        }
    };

    const handleAddMembers = async () => {
        if (!selectedGroup || selectedClients.length === 0) return;
        try {
            await groupService.addMembersToGroup(selectedGroup.id, selectedClients);
            setSelectedClients([]);
            const membersData = await groupService.getActiveMembers(selectedGroup.id);
            setMembers(membersData);
            toast.success('Üyeler eklendi');
        } catch (error) {
            toast.error(error.message || 'Üyeler eklenemedi.');
        }
    };

    const handleRemoveMember = async (customerId) => {
        if (!selectedGroup) return;
        try {
            await groupService.removeMemberFromGroup(selectedGroup.id, customerId);
            setMembers(prev => prev.filter(m => m.customerId !== customerId));
            toast.success('Üye kaldırıldı');
        } catch (error) {
            toast.error(error.message || 'Üye çıkarılamadı.');
        }
    };

    const handleClientSelection = (clientId) => {
        setSelectedClients(prev => 
            prev.includes(clientId) 
                ? prev.filter(id => id !== clientId) 
                : [...prev, clientId]
        );
    };

    const nonMemberClients = allClients
        .filter(client => !members.some(member => member.customerId === client.id))
        .filter(client => 
            client.gorunenAd.toLowerCase().includes(searchTerm.toLowerCase()) ||
            client.musteriKodu.toLowerCase().includes(searchTerm.toLowerCase())
        );
    const handleNavigateToClient = (customerId) => {
        window.open(`/dashboard/clients/${customerId}`, '_blank');
    };

    if (loading) return <div>Yükleniyor...</div>;
    return (
        <div style={styles.page}>
            <div style={styles.panel}>
                <h1 style={styles.header}>Portföy Grupları</h1>
                <form onSubmit={handleSubmit(handleCreateGroup)}>
                    <div style={styles.formGroup}>
                        <div style={{ flexGrow: 1 }}>
                            <input 
                                {...register('groupName')} 
                                placeholder="Yeni Grup Adı" 
                                style={{...styles.input, width: '100%', borderColor: errors.groupName ? 'red' : '#ccc'}} 
                            />
                            {errors.groupName && <p style={styles.errorMessage}>{errors.groupName.message}</p>}
                        </div>
                        <button type="submit" style={{...styles.button, ...styles.submitButton}} disabled={isSubmitting}>
                            {isSubmitting ? '...' : 'Oluştur'}
                        </button>
                    </div>
                </form>
                <ul style={styles.list}>
                    {groups.map(group => (
                        <li 
                            key={group.id} 
                            style={{...styles.listItem, ...(selectedGroup?.id === group.id && styles.activeListItem)}}
                            onClick={() => setSelectedGroup(group)}
                        >
                            {group.groupName}
                        </li>
                    ))}
                </ul>
            </div>


            <div style={styles.panel}>
                <h1 style={styles.header}>{selectedGroup ? `${selectedGroup.groupName} Üyeleri` : 'Grup Seçiniz'}</h1>
                <ul style={styles.list}>
                    {members.map(member => (
                        <li key={member.customerId} style={styles.customerListItem}>
                            <span 
                                onClick={() => handleNavigateToClient(member.customerId)} 
                                style={{ cursor: 'pointer', textDecoration: 'underline' }}
                                title="Müşteri detayını yeni sekmede aç"
                            >
                                {member.fullName} ({member.customerCode})
                            </span>
                            <button onClick={() => handleRemoveMember(member.customerId)} style={{...styles.button, ...styles.removeButton}}>Kaldır</button>
                        </li>
                    ))}
                </ul>
            </div>


            <div style={styles.panel}>
                <h1 style={styles.header}>Müşteriler</h1>
                
                <input 
                    type="text"
                    placeholder="Müşteri ara (Ad veya Kod)..."
                    style={{...styles.input, marginBottom: '15px'}}
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
                
                <button 
                    onClick={handleAddMembers} 
                    style={{...styles.button, ...styles.submitButton, marginBottom: '20px', alignSelf: 'flex-end'}}
                    disabled={selectedClients.length === 0 || !selectedGroup}
                >
                    Seçilenleri Gruba Ekle
                </button>
                
                <ul style={styles.list}>
                    {nonMemberClients.map(client => (
                        <li key={client.id} style={styles.customerListItem}>
                            <input 
                                type="checkbox" 
                                style={styles.checkbox}
                                checked={selectedClients.includes(client.id)}
                                onChange={() => handleClientSelection(client.id)}
                            />
                           <span 
                                onClick={() => handleNavigateToClient(client.id)}
                                style={{ cursor: 'pointer', textDecoration: 'underline' }}
                                title="Müşteri detayını yeni sekmede aç"
                            >
                                {client.gorunenAd} ({client.musteriKodu})
                           </span>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}
