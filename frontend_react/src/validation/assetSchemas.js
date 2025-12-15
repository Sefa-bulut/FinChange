import * as yup from 'yup';

export const assetSchema = yup.object().shape({
    isinCode: yup.string()
        .required('ISIN Kodu zorunludur.')
        .max(12, 'ISIN Kodu en fazla 12 karakter olabilir.'),

    bistCode: yup.string()
        .required('BIST Kodu zorunludur.')
        .max(10, 'BIST Kodu en fazla 10 karakter olabilir.'),

    companyName: yup.string()
        .required('Şirket Adı zorunludur.'),

    sector: yup.string().nullable(), // nullable() boş olmasına izin verir

    currency: yup.string()
        .required('Para Birimi zorunludur.')
        .length(3, 'Para Birimi 3 karakter olmalıdır.'),

    settlementDays: yup.number()
        .typeError('Lütfen geçerli bir sayı girin.')
        .required('Takas süresi zorunludur.')
        .positive('Takas süresi pozitif bir sayı olmalıdır.')
        .integer('Takas süresi bir tam sayı olmalıdır.'),

    maxOrderValue: yup.number()
        .transform(value => (isNaN(value) || value === null || value === '') ? undefined : value)
        .nullable()
        .positive('Maksimum emir değeri pozitif bir sayı olmalıdır.'),
});