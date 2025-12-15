import * as yup from 'yup';

// Dokümandaki kurallara göre login formu validasyonu
export const loginSchema = yup.object().shape({
    email: yup.string()
        .email('Geçersiz e-posta formatı.')
        .required('E-posta adresi zorunludur.'),
    password: yup.string()
        .required('Şifre zorunludur.'),
});

// Yeni personel davet formu validasyonu
export const inviteUserSchema = yup.object().shape({
    firstName: yup.string()
        .matches(/^[a-zA-ZğüşıöçĞÜŞİÖÇ\s]+$/, 'Ad sadece harf içerebilir.')
        .min(2, 'Ad en az 2 karakter olmalıdır.')
        .required('Ad zorunludur.'),
    lastName: yup.string()
        .matches(/^[a-zA-ZğüşıöçĞÜŞİÖÇ\s]+$/, 'Soyad sadece harf içerebilir.')
        .min(2, 'Soyad en az 2 karakter olmalıdır.')
        .required('Soyad zorunludur.'),
    email: yup.string()
        .email('Geçersiz e-posta formatı.')
        .required('E-posta adresi zorunludur.'),
    personnelCode: yup.string()
        .required('Personel kodu zorunludur.'),
    roleNames: yup.array()
        .min(1, 'En az bir rol seçilmelidir.')
        .required('Rol seçimi zorunludur.'),
});

// Şifre değiştirme/sıfırlama formu validasyonu
export const passwordSchema = yup.object().shape({
    newPassword: yup.string()
        .min(6, 'Şifre en az 6 karakter olmalıdır.')
        .required('Yeni şifre zorunludur.'),
    confirmPassword: yup.string()
        .oneOf([yup.ref('newPassword'), null], 'Şifreler uyuşmuyor.')
        .required('Şifre tekrarı zorunludur.'),
});

// Şifremi unuttum formu validasyonu
export const forgotPasswordSchema = yup.object().shape({
    email: yup.string()
        .email('Geçersiz e-posta formatı.')
        .required('E-posta adresi zorunludur.'),
});

// Şifre sıfırlama formu validasyonu (token + yeni şifre)
export const resetPasswordSchema = yup.object().shape({
    token: yup.string()
        .required('Sıfırlama kodu zorunludur.'),
    newPassword: yup.string()
        .min(6, 'Şifre en az 6 karakter olmalıdır.')
        .required('Yeni şifre zorunludur.'),
    confirmPassword: yup.string()
        .oneOf([yup.ref('newPassword'), null], 'Şifreler uyuşmuyor.')
        .required('Şifre tekrarı zorunludur.'),
});

// Personel güncelleme formu validasyonu
export const updateUserSchema = yup.object().shape({
    firstName: yup.string()
        .matches(/^[a-zA-ZğüşıöçĞÜŞİÖÇ\s]+$/, 'Ad sadece harf içerebilir.')
        .min(2, 'Ad en az 2 karakter olmalıdır.')
        .required('Ad zorunludur.'),
    lastName: yup.string()
        .matches(/^[a-zA-ZğüşıöçĞÜŞİÖÇ\s]+$/, 'Soyad sadece harf içerebilir.')
        .min(2, 'Soyad en az 2 karakter olmalıdır.')
        .required('Soyad zorunludur.'),
    personnelCode: yup.string()
        .required('Personel kodu zorunludur.'),
    roleNames: yup.array()
        .min(1, 'En az bir rol seçilmelidir.')
        .required('Rol seçimi zorunludur.'),
    isActive: yup.boolean()
        .required('Durum seçimi zorunludur.'),
});