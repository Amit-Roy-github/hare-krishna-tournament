import axiosClient from './axiosClient';

export const getKrishnadasList   = async ()           => axiosClient.get('/krishnadas');
export const createKrishnadas    = async (data)        => axiosClient.post('/krishnadas', data);
export const updateKrishnadas    = async (id, fields)  => axiosClient.patch('/krishnadas', { id, ...fields });
export const deleteKrishnadas    = async (id)          => axiosClient.delete('/krishnadas', { data: { id } });
