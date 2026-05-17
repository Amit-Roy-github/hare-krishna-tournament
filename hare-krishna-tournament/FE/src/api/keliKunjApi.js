import axiosClient from './axiosClient';

export const getKeliKunjList   = async ()        => axiosClient.get('/keliKunj');
export const createKeliKunj    = async (data)     => axiosClient.post('/keliKunj', data);
export const updateKeliKunj    = async (id, data) => axiosClient.patch('/keliKunj', { id, ...data });
