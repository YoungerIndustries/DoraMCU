/**
 * DoraMCU Game UI - Core Shared State & Utilities
 *
 * This module provides the shared state object and utility functions
 * used by all other game-ui modules. Load this file first.
 */

/** @type {Object} Shared mutable state accessible by all sub-modules */
const GameUIState = {
    gameState: null,
    sessionId: null,
    selectedFleet: null,
    selectedPlanet: null,
    selectedSubSector: null,

    // Turn phase state
    currentPhase: 0,
    PHASES: ['resource', 'shipBuild', 'fleetMove', 'combat'],
    PHASE_NAMES: {
        resource: '⚡ Resource Collection',
        shipBuild: '🔨 Ship Building',
        fleetMove: '🎯 Fleet Movement',
        combat: '⚔️ Combat Phase'
    },
    PHASE_MODAL_IDS: {
        resource: 'resourcePhaseModal',
        shipBuild: 'shipBuildPhaseModal',
        fleetMove: 'fleetMovePhaseModal',
        combat: 'combatPhaseModal'
    },

    // Turn timer state
    turnTimerInterval: null,
    turnTimerRemaining: 0,
    turnTimerTotal: 5,

    // DOM elements cache
    elements: {},

    // Galaxy creation progress
    galaxyProgressEventSource: null,

    // Enhanced UI state
    commandBarVisible: false,
    commandBarSelectedIndex: 0,

    // Current system being viewed (for trade actions)
    currentSystemId: null,

    // Game state poll timer
    gameStatePollTimer: null,

    // Galaxy map state
    galaxyData: null,
    mapScale: 1,
    mapOffsetX: 0,
    mapOffsetY: 0,
    isDragging: false,
    dragStartX: 0,
    dragStartY: 0,
    dragOffsetStartX: 0,
    dragOffsetStartY: 0,
    hoveredSystem: null,
    selectedSystem: null,
    hoveredSubSector: null,
    selectedSectorRow: null,
    selectedSectorCol: null,
    selectedSubSectorRow: null,
    selectedSubSectorCol: null,
    sectorCardsContainer: null,
    subsectorCardsContainer: null,
    systemDetailContainer: null,
    subsectorGraphicContainer: null,
    subsectorSystemList: null,
};

/** @type {Object} Public API surface — sub-modules register their functions here */
const GameUI = {
    state: GameUIState
};

/**
 * Escape HTML special characters to prevent XSS.
 * @param {string} str
 * @returns {string}
 */
GameUI.escapeHtml = function(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
};

/**
 * Hide a modal by ID.
 * @param {string} id
 */
GameUI.hideModal = function(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('hidden');
};

/**
 * Show a toast notification.
 * @param {string} message
 * @param {string} [type='info']
 */
GameUI.showToast = function(message, type) {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const icons = {
        success: '✅',
        error: '❌',
        warning: '⚠️',
        info: 'ℹ️'
    };

    const toast = document.createElement('div');
    toast.className = `toast ${type || 'info'}`;
    toast.innerHTML = `
        <span class="toast-icon">${icons[type] || 'ℹ️'}</span>
        <div class="toast-content">
            <div class="toast-message">${GameUI.escapeHtml(message)}</div>
        </div>
        <button class="toast-close">&times;</button>
        <div class="toast-progress"></div>
    `;

    toast.querySelector('.toast-close').addEventListener('click', () => {
        GameUI.removeToast(toast);
    });

    container.appendChild(toast);

    setTimeout(() => {
        GameUI.removeToast(toast);
    }, 4000);
};

/**
 * Remove a toast element with animation.
 * @param {HTMLElement} toast
 */
GameUI.removeToast = function(toast) {
    if (!toast || !toast.parentNode) return;
    toast.style.animation = 'toastSlideOut 0.3s ease-in forwards';
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 300);
};

/**
 * Show the command bar overlay.
 */
GameUI.showCommandBar = function() {
    const commandBar = document.getElementById('commandBar');
    const input = document.getElementById('commandBarInput');
    if (commandBar) {
        commandBar.classList.remove('hidden');
        GameUIState.commandBarVisible = true;
        if (input) {
            input.value = '';
            input.focus();
            const items = commandBar.querySelectorAll('.command-bar-item');
            items.forEach(item => item.style.display = 'flex');
        }
    }
};

/**
 * Hide the command bar overlay.
 */
GameUI.hideCommandBar = function() {
    const commandBar = document.getElementById('commandBar');
    if (commandBar) {
        commandBar.classList.add('hidden');
        GameUIState.commandBarVisible = false;
    }
};

/**
 * Animate a resource value change.
 * @param {string} resourceId
 * @param {number} oldValue
 * @param {number} newValue
 */
GameUI.updateResourceChange = function(resourceId, oldValue, newValue) {
    const changeEl = document.getElementById(resourceId + 'Change');
    if (!changeEl) return;

    const diff = newValue - oldValue;
    if (diff === 0) {
        changeEl.textContent = '+0';
        changeEl.className = 'resource-change positive';
        return;
    }

    const sign = diff > 0 ? '+' : '';
    changeEl.textContent = sign + diff;
    changeEl.className = `resource-change ${diff > 0 ? 'positive' : 'negative'}`;

    changeEl.style.transform = 'scale(1.3)';
    changeEl.style.opacity = '1';
    setTimeout(() => {
        changeEl.style.transform = 'scale(1)';
    }, 300);
};
