import React, { useState } from 'react';
// Kullanılacak yeni ikonları (FaCog, FaCalendarAlt, FaClock) import edelim
import { 
    FaUsers, FaFileInvoice, FaChartBar, FaExchangeAlt, FaChevronDown, 
    FaChevronRight, FaUserPlus, FaUserEdit, FaList, FaUsersCog, 
    FaFileAlt, FaDesktop, FaPlusCircle, FaCommentDots, FaBuilding, FaBookmark,
    FaCog, FaCalendarAlt, FaClock
} from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import logo from '../../assets/images/finchange-logo.png';
import { useAuth } from '../../context/AuthContext';

// Stil tanımlamaları (değişiklik yok)
const styles = {
  sidebar: {
    width: '250px',
    backgroundColor: '#212529',
    color: '#adb5bd',
    display: 'flex',
    flexDirection: 'column',
    height: '100vh',
    overflowY: 'auto'
  },
  logoContainer: {
    padding: '20px',
    textAlign: 'center',
    borderBottom: '1px solid #495057',
  },
  logo: {
    maxWidth: '80%',
    height: 'auto'
  },
  menu: {
    listStyle: 'none',
    padding: '20px 0',
    margin: 0,
    flexGrow: 1,
  },
  menuItem: {
    padding: '15px 20px',
    display: 'flex',
    alignItems: 'center',
    cursor: 'pointer',
    transition: 'background-color 0.2s, color 0.2s',
    userSelect: 'none'
  },
  menuItemHover: {
    backgroundColor: '#343a40',
    color: '#ffffff'
  },
  menuIcon: {
    marginRight: '15px',
    fontSize: '18px',
    width: '20px',
    textAlign: 'center'
  },
  dropdownIcon: {
    marginLeft: 'auto',
    fontSize: '14px',
    transition: 'transform 0.2s',
  },
  submenu: {
    backgroundColor: '#1a1e21',
    paddingLeft: '20px',
  },
  submenuItem: {
    padding: '12px 20px',
    display: 'flex',
    alignItems: 'center',
    cursor: 'pointer',
    fontSize: '14px',
    transition: 'background-color 0.2s, color 0.2s',
  },
  submenuItemHover: {
    backgroundColor: '#2c3034',
    color: '#ffffff'
  },
};

// Menü konfigürasyonunu yönetmek için merkezi bir yapı
const menuConfig = [
  {
    name: 'Personel İşlemleri',
    icon: <FaUsersCog />,
    requiredPermission: 'user:read:all',
    isDropdown: true,
    submenu: [
      { name: 'Personel Kayıt', icon: <FaUserPlus />, path: '/dashboard/users/new', requiredPermission: 'user:create' },
      { name: 'Personel Listesi', icon: <FaList />, path: '/dashboard/users', requiredPermission: 'user:read:all' }
    ]
  },
  {
    name: 'Müşteri İşlemleri',
    icon: <FaBuilding />,
    requiredPermission: 'client:read:all',
    isDropdown: true,
    submenu: [
      { name: 'Müşteri Listesi', icon: <FaList />, path: '/dashboard/clients', requiredPermission: 'client:read:all' },
      { name: 'Yeni Müşteri', icon: <FaUserPlus />, path: '/dashboard/clients/new', requiredPermission: 'client:create' },
      { name: 'Grup Yönetimi', icon: <FaUsers />, path: '/dashboard/portfolio-groups', requiredPermission: 'client:create' },
      { name: 'Hesap Yönetimi', icon: <FaFileAlt />, path: '/dashboard/account-records', requiredPermission: 'client:read:all' }
    ]
  },
  {
    name: 'Varlık Yönetimi',
    icon: <FaChartBar />,
    requiredPermission: 'client:read:all',
    isDropdown: true,
    submenu: [
      { name: 'Varlık Tanımla', icon: <FaPlusCircle />, path: '/dashboard/assets/create', requiredPermission: 'client:create' },
      { name: 'Varlık Listesi', icon: <FaList />, path: '/dashboard/assets/list', requiredPermission: 'client:read:all' },
      { name: 'Aracı Kurum Yönetimi', icon: <FaBuilding />, path: '/dashboard/brokerage-firms', requiredPermission: 'user:read:all' }
    ]
  },
  {
    name: 'Piyasa Ekranları',
    icon: <FaDesktop />,
    requiredPermission: 'order:read:own',
    isDropdown: true,
    submenu: [
      { name: 'Tüm Piyasa', icon: <FaChartBar />, path: '/dashboard/market-watch', requiredPermission: 'order:read:own' },
      { name: 'Kişisel İzleme', icon: <FaBookmark />, path: '/dashboard/market-data', requiredPermission: 'order:read:own' },
    ]
  },
  {
    name: 'Hisse İşlemleri',
    icon: <FaExchangeAlt />,
    requiredPermission: 'order:create',
    isDropdown: true,
    submenu: [
      { name: 'Alım/Satım', icon: <FaExchangeAlt />, path: '/dashboard/trading', requiredPermission: 'order:create' },
      { name: 'Emir Listesi', icon: <FaList />, path: '/dashboard/orders', requiredPermission: 'order:read:own' },
    ]
  },
  {
    name: 'Raporlama',
    icon: <FaChartBar />,
    path: '/dashboard/reports',
    requiredPermission: 'client:read:all'
  },

  {
    name: 'Finbot',
    icon: <FaCommentDots />,
    path: '/dashboard/finbot'
  },
  {
    name: 'Sistem Yönetimi',
    icon: <FaCog />,
    requiredPermission: 'order:create',
    isDropdown: true,
    submenu: [
      { 
        name: 'Tatil Yönetimi', 
        icon: <FaCalendarAlt />,
        path: '/dashboard/holidays',
        requiredPermission: 'order:create'
      },
      { 
        name: 'Sistem Tarihi', 
        icon: <FaClock />,
        path: '/dashboard/system-date',
        requiredPermission: 'order:create'
      },
    ]
  }
];

export default function Sidebar() {
  const { permissions } = useAuth();
  const navigate = useNavigate();
  const [openDropdowns, setOpenDropdowns] = useState({});

  const toggleDropdown = (itemName) => {
    setOpenDropdowns(prev => ({
      ...prev,
      [itemName]: !prev[itemName]
    }));
  };

  const handleNavigation = (path) => {
    if (path) {
      navigate(path);
    }
  };

  const handleMouseEnter = (e) => {
    e.currentTarget.style.backgroundColor = styles.menuItemHover.backgroundColor;
    e.currentTarget.style.color = styles.menuItemHover.color;
  };

  const handleMouseLeave = (e) => {
    e.currentTarget.style.backgroundColor = 'transparent';
    e.currentTarget.style.color = '#adb5bd';
  };
  
  const handleSubMenuMouseEnter = (e) => {
    e.currentTarget.style.backgroundColor = styles.submenuItemHover.backgroundColor;
    e.currentTarget.style.color = styles.submenuItemHover.color;
  };
  
  const handleSubMenuMouseLeave = (e) => {
    e.currentTarget.style.backgroundColor = 'transparent';
    e.currentTarget.style.color = '#adb5bd';
  };


  return (
      <div style={styles.sidebar}>
          <div style={styles.logoContainer}>
              <img src={logo} alt="Finchange Logo" style={styles.logo} />
          </div>
          <ul style={styles.menu}>
              {menuConfig.map(item => {
                  if (item.name !== 'Finbot' && item.requiredPermission && !permissions.includes(item.requiredPermission)) {
                      return null;
                  }

                  if (item.isDropdown) {
                      const isOpen = openDropdowns[item.name];

                      return (
                          <React.Fragment key={item.name}>
                              <li
                                  style={styles.menuItem}
                                  onClick={() => toggleDropdown(item.name)}
                                  onMouseEnter={handleMouseEnter}
                                  onMouseLeave={handleMouseLeave}
                              >
                                  {item.icon && React.cloneElement(item.icon, { style: styles.menuIcon })}
                                  <span>{item.name}</span>
                                  {isOpen ?
                                      <FaChevronDown style={styles.dropdownIcon} /> :
                                      <FaChevronRight style={styles.dropdownIcon} />
                                  }
                              </li>
                              {isOpen && (
                                  <div style={styles.submenu}>
                                      {item.submenu.map(subItem => {
                                          if (subItem.requiredPermission && !permissions.includes(subItem.requiredPermission)) {
                                              return null;
                                          }

                                          return (
                                              <div
                                                  key={subItem.name}
                                                  style={styles.submenuItem}
                                                  onClick={() => handleNavigation(subItem.path)}
                                                  onMouseEnter={handleSubMenuMouseEnter}
                                                  onMouseLeave={handleSubMenuMouseLeave}
                                              >
                                                  {subItem.icon && React.cloneElement(subItem.icon, { style: styles.menuIcon })}
                                                  <span>{subItem.name}</span>
                                              </div>
                                          );
                                      })}
                                  </div>
                              )}
                          </React.Fragment>
                      );
                  }

                  return (
                      <li
                          key={item.name}
                          style={styles.menuItem}
                          onClick={() => handleNavigation(item.path)}
                          onMouseEnter={handleMouseEnter}
                          onMouseLeave={handleMouseLeave}
                      >
                          {item.icon && React.cloneElement(item.icon, { style: styles.menuIcon })}
                          <span>{item.name}</span>
                      </li>
                  );
              })}
          </ul>
      </div>
  );
}