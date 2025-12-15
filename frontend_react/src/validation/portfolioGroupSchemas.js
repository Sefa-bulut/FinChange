import * as yup from 'yup';

// Yeni portföy grubu oluşturma formu için validasyon şeması
export const createGroupSchema = yup.object().shape({
    groupName: yup.string()
        .trim() // Başındaki ve sonundaki boşlukları temizle
        .min(3, 'Grup adı en az 3 karakter olmalıdır.')
        .max(100, 'Grup adı en fazla 100 karakter olabilir.')
        .required('Grup adı zorunludur.'),
});