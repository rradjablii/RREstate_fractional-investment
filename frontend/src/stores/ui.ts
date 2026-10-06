import { create } from 'zustand'

interface UiState { sidebarOpen: boolean; setSidebarOpen: (v: boolean) => void }
export const useUi = create<UiState>((set) => ({ sidebarOpen: false, setSidebarOpen: (sidebarOpen) => set({ sidebarOpen }) }))
