import axiosClient from './axiosClient';

export const getScores    = async ()       => axiosClient.get('/scores');
export const updateScores = async (updates) => axiosClient.post('/scores', updates);
