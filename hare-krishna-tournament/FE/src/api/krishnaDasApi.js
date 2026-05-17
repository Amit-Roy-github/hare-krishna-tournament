import axiosClient from './axiosClient';

export const getKrishnaDasList   = async ()           => axiosClient.get('/krishnaDas');
export const createKrishnaDas    = async (data)        => axiosClient.post('/krishnaDas', data);
export const updateKrishnaDas    = async (id, fields)  => axiosClient.patch('/krishnaDas', { id, ...fields });
export const deleteKrishnaDas    = async (id)          => axiosClient.delete('/krishnaDas', { data: { id } });
