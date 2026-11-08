/* DoraMCU - Psionics UI Module (B03 Worlds & Adventures) */

const PsionicUI = (function() {
    let currentCharacterId = null;
    let psionicProfile = null;
    let disciplines = [];
    let difficulties = [];
    let encounterTypes = [];

    function showPsionicModal(characterId) {
        currentCharacterId = characterId;
        const modal = document.getElementById('psionicModal');
        if (modal) {
            modal.classList.remove('hidden');
            loadAllPsionicData();
        }
    }

    function hidePsionicModal() {
        const modal = document.getElementById('psionicModal');
        if (modal) modal.classList.add('hidden');
    }

    async function loadAllPsionicData() {
        try {
            // Load reference data
            try {
                disciplines = await GameAPI.getPsionicDisciplines();
            } catch (e) {
                disciplines = [];
            }
            try {
                difficulties = await GameAPI.getPsionicDifficulties();
            } catch (e) {
                difficulties = [];
            }
            try {
                encounterTypes = await GameAPI.getPsionicEncounterTypes();
            } catch (e) {
                encounterTypes = [];
            }

            // Load character profile
            if (currentCharacterId) {
                try {
                    psionicProfile = await GameAPI.getPsionicProfile(currentCharacterId);
                } catch (e) {
                    psionicProfile = null;
                }
            }

            renderAllPanels();
        } catch (e) {
            console.error('Psionic data load failed:', e);
        }
    }

    function renderAllPanels() {
        renderDetectionPanel();
        renderDisciplinesPanel();
        renderPowerCheckPanel();
        renderEncounterPanel();
    }

    function switchTab(tabName) {
        // Hide all panels
        document.querySelectorAll('.psionic-panel').forEach(p => p.classList.add('hidden'));
        // Deactivate all tabs
        document.querySelectorAll('.psionic-tab').forEach(t => {
            t.style.background = 'rgba(255,255,255,0.05)';
            t.style.color = 'var(--text-secondary)';
            t.classList.remove('active');
        });
        // Show selected panel
        const panelMap = {
            'detection': 'detectionPanel',
            'disciplines': 'disciplinesPanel',
            'powercheck': 'powerCheckPanel',
            'encounter': 'encounterPanel'
        };
        const panel = document.getElementById(panelMap[tabName]);
        if (panel) panel.classList.remove('hidden');
        // Activate selected tab
        const tab = document.querySelector(`.psionic-tab[data-tab="${tabName}"]`);
        if (tab) {
            tab.style.background = 'rgba(167,139,250,0.2)';
            tab.style.color = '#a78bfa';
            tab.classList.add('active');
        }
    }

    function renderDetectionPanel() {
        const panel = document.getElementById('detectionPanel');
        if (!panel) return;

        if (!psionicProfile) {
            panel.innerHTML = `
                <div style="text-align:center; padding:20px; color:var(--text-secondary);">
                    <div style="font-size:2rem; margin-bottom:8px;">🔮</div>
                    <div>Select a character to check for psionic ability</div>
                </div>
            `;
            return;
        }

        const inScore = psionicProfile.inScore || 0;
        const isPsionic = psionicProfile.isPsionic || false;
        const level = psionicProfile.detectionLevelDisplay || 'None';
        const fatigue = psionicProfile.fatigue || 0;
        const maxFatigue = psionicProfile.maxFatigue || 0;
        const canUse = psionicProfile.canUsePowers || false;

        const levelColors = {
            'None': '#6b7280',
            'Latent': '#a78bfa',
            'Weak': '#60a5fa',
            'Average': '#34d399',
            'Strong': '#fbbf24',
            'Master': '#f472b6'
        };
        const levelColor = levelColors[level] || '#6b7280';

        panel.innerHTML = `
            <div style="padding:12px; background:rgba(255,255,255,0.03); border-radius:8px; margin-bottom:12px;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                    <h4 style="margin:0; color:var(--text-primary);">🔮 ${psionicProfile.characterName || 'Unknown'}</h4>
                    <span style="font-size:0.8rem; color:var(--text-secondary);">In: ${inScore}</span>
                </div>
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px; margin-bottom:12px;">
                    <div style="padding:8px; background:rgba(255,255,255,0.05); border-radius:6px; text-align:center;">
                        <div style="font-size:0.7rem; color:var(--text-secondary); margin-bottom:4px;">Detection Level</div>
                        <div style="font-size:1.2rem; font-weight:700; color:${levelColor};">${level}</div>
                    </div>
                    <div style="padding:8px; background:rgba(255,255,255,0.05); border-radius:6px; text-align:center;">
                        <div style="font-size:0.7rem; color:var(--text-secondary); margin-bottom:4px;">Fatigue</div>
                        <div style="font-size:1.2rem; font-weight:700; color:${fatigue >= maxFatigue ? '#ef4444' : '#34d399'};">${fatigue}/${maxFatigue}</div>
                    </div>
                </div>
                <div style="display:flex; gap:8px; flex-wrap:wrap;">
                    <button class="btn-small primary" onclick="PsionicUI.detectPsionic()" style="flex:1;">
                        🎲 Detect Psionic
                    </button>
                    ${isPsionic ? `
                        <button class="btn-small" onclick="PsionicUI.restPsionic()" style="flex:1;">
                            😴 Rest (1h)
                        </button>
                    ` : ''}
                </div>
                <div id="detectionResult" style="margin-top:8px;"></div>
            </div>
            <div style="font-size:0.75rem; color:var(--text-secondary); padding:8px; background:rgba(255,255,255,0.03); border-radius:6px;">
                <strong>How it works:</strong> Characters with In (Intuition) 8+ may have psionic potential.
                Roll 2D6 and compare against In score. Lower rolls indicate stronger psionic ability.
                Powers cause fatigue; rest removes 1 fatigue per hour.
            </div>
        `;
    }

    function renderDisciplinesPanel() {
        const panel = document.getElementById('disciplinesPanel');
        if (!panel) return;

        const charDisciplines = (psionicProfile && psionicProfile.disciplines) || [];

        let html = '<div style="margin-bottom:12px;">';
        html += '<h4 style="margin:0 0 8px 0; color:var(--text-primary);">📚 Psionic Disciplines</h4>';
        html += '<div style="display:grid; gap:6px;">';

        for (const disc of disciplines) {
            const charLevel = charDisciplines.find(d => d.name === disc.name);
            const level = charLevel ? charLevel.level : 0;
            const levelStars = '★'.repeat(level) + '☆'.repeat(5 - level);
            const bgColor = level > 0 ? 'rgba(167,139,250,0.15)' : 'rgba(255,255,255,0.03)';

            html += `
                <div style="padding:8px; background:${bgColor}; border-radius:6px; display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <div style="font-weight:600; font-size:0.85rem; color:var(--text-primary);">${disc.displayName}</div>
                        <div style="font-size:0.7rem; color:var(--text-secondary);">${disc.description}</div>
                    </div>
                    <div style="text-align:right;">
                        <div style="color:#fbbf24; font-size:0.9rem;">${levelStars}</div>
                        ${psionicProfile && psionicProfile.isPsionic ? `
                            <button class="btn-small" onclick="PsionicUI.addDiscipline('${disc.name}')"
                                style="margin-top:4px; padding:2px 8px; font-size:0.7rem;">
                                + Train
                            </button>
                        ` : ''}
                    </div>
                </div>
            `;
        }

        html += '</div></div>';
        panel.innerHTML = html;
    }

    function renderPowerCheckPanel() {
        const panel = document.getElementById('powerCheckPanel');
        if (!panel) return;

        const inScore = (psionicProfile && psionicProfile.inScore) || 8;
        const charDisciplines = (psionicProfile && psionicProfile.disciplines) || [];

        let html = '<div style="padding:12px; background:rgba(255,255,255,0.03); border-radius:8px;">';
        html += '<h4 style="margin:0 0 8px 0; color:var(--text-primary);">⚡ Power Check</h4>';

        // Discipline selector
        html += '<div class="form-group" style="margin-bottom:8px;">';
        html += '<label style="font-size:0.75rem; color:var(--text-secondary);">Discipline</label>';
        html += '<select class="form-input" id="powerCheckDiscipline" style="width:100%;">';
        if (charDisciplines.length > 0) {
            for (const disc of charDisciplines) {
                html += `<option value="${disc.level}">${disc.displayName} (Level ${disc.level})</option>`;
            }
        } else {
            html += '<option value="1">Level 1 (default)</option>';
            html += '<option value="2">Level 2</option>';
            html += '<option value="3">Level 3</option>';
            html += '<option value="4">Level 4</option>';
            html += '<option value="5">Level 5</option>';
        }
        html += '</select></div>';

        // Difficulty selector
        html += '<div class="form-group" style="margin-bottom:8px;">';
        html += '<label style="font-size:0.75rem; color:var(--text-secondary);">Difficulty</label>';
        html += '<select class="form-input" id="powerCheckDifficulty" style="width:100%;">';
        for (const diff of difficulties) {
            html += `<option value="${diff.name}">${diff.displayName} (${diff.modifier > 0 ? '+' : ''}${diff.modifier})</option>`;
        }
        html += '</select></div>';

        // In score display
        html += `<div style="font-size:0.75rem; color:var(--text-secondary); margin-bottom:8px;">`;
        html += `In Score: <strong style="color:var(--accent-cyan);">${inScore}</strong>`;
        html += '</div>';

        // Check button
        html += '<button class="btn-small primary" onclick="PsionicUI.performPowerCheck()" style="width:100%;">';
        html += '🎲 Perform Power Check</button>';

        // Result area
        html += '<div id="powerCheckResult" style="margin-top:8px;"></div>';
        html += '</div>';

        // Formula explanation
        html += '<div style="font-size:0.7rem; color:var(--text-secondary); padding:8px; background:rgba(255,255,255,0.03); border-radius:6px; margin-top:8px;">';
        html += '<strong>Formula:</strong> Target = 12 - (In/2) - disciplineLevel + difficulty<br>';
        html += 'Success = roll 2D6 <= target. Failed attempts cost 1 fatigue; success costs 1 + difficulty/2.';
        html += '</div>';

        panel.innerHTML = html;
    }

    function renderEncounterPanel() {
        const panel = document.getElementById('encounterPanel');
        if (!panel) return;

        const isPsionic = psionicProfile && psionicProfile.isPsionic;
        const level = (psionicProfile && psionicProfile.detectionLevel) || 'WEAK';

        let html = '<div style="padding:12px; background:rgba(255,255,255,0.03); border-radius:8px;">';
        html += '<h4 style="margin:0 0 8px 0; color:var(--text-primary);">🌀 Psionic Encounter</h4>';

        if (isPsionic) {
            html += `<div style="font-size:0.75rem; color:var(--text-secondary); margin-bottom:8px;">`;
            html += `Detection Level: <strong style="color:#a78bfa;">${psionicProfile.detectionLevelDisplay || 'Weak'}</strong>`;
            html += '</div>';
        } else {
            html += '<div style="font-size:0.75rem; color:var(--text-secondary); margin-bottom:8px;">';
            html += 'No psionic ability detected. Non-psionics have a 16% encounter chance.';
            html += '</div>';
        }

        // Encounter chance info
        html += '<div style="font-size:0.7rem; color:var(--text-secondary); padding:6px; background:rgba(255,255,255,0.05); border-radius:4px; margin-bottom:8px;">';
        html += isPsionic
            ? '🔮 Psionic encounter chance: 67% (roll 2D6 ≤ 7)'
            : '👤 Non-psionic encounter chance: 16% (roll 2D6 = 2)';
        html += '</div>';

        // Encounter button
        html += '<button class="btn-small primary" onclick="PsionicUI.generateEncounter(\'' + level + '\')" style="width:100%;">';
        html += '🎲 Check for Encounter</button>';

        // Result area
        html += '<div id="encounterResult" style="margin-top:8px;"></div>';
        html += '</div>';

        // Encounter types reference
        html += '<div style="margin-top:12px;">';
        html += '<h4 style="margin:0 0 8px 0; color:var(--text-primary); font-size:0.85rem;">Encounter Types</h4>';
        html += '<div style="display:grid; gap:4px;">';
        const encounterIcons = {
            'DETECTION': '👁️', 'ALLY': '🤝', 'HOSTILE': '⚔️', 'VISION': '👁️‍🗨️',
            'ENTITY': '👻', 'SURGE': '⚡', 'SUPPRESSION': '🔇', 'ARTIFACT': '💎'
        };
        for (const enc of encounterTypes) {
            const icon = encounterIcons[enc.name] || '🌀';
            html += `<div style="font-size:0.75rem; padding:4px 8px; background:rgba(255,255,255,0.03); border-radius:4px;">`;
            html += `<span>${icon}</span> <strong>${enc.displayName}</strong>: ${enc.description}`;
            html += '</div>';
        }
        html += '</div></div>';

        panel.innerHTML = html;
    }

    // ==================== Action Handlers ====================

    async function detectPsionic() {
        if (!currentCharacterId) {
            showToast('No character selected', 'warning');
            return;
        }
        try {
            const result = await GameAPI.detectPsionic(currentCharacterId);
            const resultDiv = document.getElementById('detectionResult');
            if (result.detected) {
                resultDiv.innerHTML = `
                    <div style="padding:8px; background:rgba(167,139,250,0.2); border-radius:6px; border:1px solid rgba(167,139,250,0.4);">
                        <div style="color:#a78bfa; font-weight:600;">✨ Psionic Ability Detected!</div>
                        <div style="font-size:0.8rem; color:var(--text-secondary);">Level: ${result.detectionLevelDisplay}</div>
                    </div>
                `;
                showToast('Psionic ability detected!', 'success');
            } else {
                resultDiv.innerHTML = `
                    <div style="padding:8px; background:rgba(255,255,255,0.05); border-radius:6px;">
                        <div style="color:var(--text-secondary);">No psionic ability detected (In: ${result.inScore})</div>
                    </div>
                `;
                showToast('No psionic ability detected', 'info');
            }
            // Reload profile
            psionicProfile = await GameAPI.getPsionicProfile(currentCharacterId);
            renderAllPanels();
        } catch (e) {
            showToast('Detection failed: ' + e.message, 'error');
        }
    }

    async function restPsionic() {
        if (!currentCharacterId) return;
        try {
            await GameAPI.restPsionic(currentCharacterId, 1);
            psionicProfile = await GameAPI.getPsionicProfile(currentCharacterId);
            renderAllPanels();
            showToast('Restored 1 fatigue point', 'success');
        } catch (e) {
            showToast('Rest failed: ' + e.message, 'error');
        }
    }

    async function addDiscipline(disciplineName) {
        if (!currentCharacterId) return;
        try {
            await GameAPI.addDiscipline(currentCharacterId, disciplineName, 1);
            psionicProfile = await GameAPI.getPsionicProfile(currentCharacterId);
            renderAllPanels();
            showToast(`${disciplineName} trained!`, 'success');
        } catch (e) {
            showToast('Failed to train discipline: ' + e.message, 'error');
        }
    }

    async function performPowerCheck() {
        const disciplineLevel = parseInt(document.getElementById('powerCheckDiscipline').value) || 1;
        const difficulty = document.getElementById('powerCheckDifficulty').value || 'NORMAL';
        const inScore = (psionicProfile && psionicProfile.inScore) || 8;

        try {
            const result = await GameAPI.performPowerCheck(inScore, disciplineLevel, difficulty);
            const resultDiv = document.getElementById('powerCheckResult');
            const bgColor = result.success ? 'rgba(52,211,153,0.2)' : 'rgba(239,68,68,0.2)';
            const borderColor = result.success ? 'rgba(52,211,153,0.4)' : 'rgba(239,68,68,0.4)';
            const textColor = result.success ? '#34d399' : '#ef4444';

            resultDiv.innerHTML = `
                <div style="padding:8px; background:${bgColor}; border-radius:6px; border:1px solid ${borderColor};">
                    <div style="color:${textColor}; font-weight:600;">${result.success ? '✅ Success!' : '❌ Failed'}</div>
                    <div style="font-size:0.8rem; color:var(--text-secondary);">
                        Rolled ${result.roll} vs target ${result.target} | Fatigue cost: ${result.fatigueCost}
                    </div>
                </div>
            `;
        } catch (e) {
            showToast('Power check failed: ' + e.message, 'error');
        }
    }

    async function generateEncounter(detectionLevel) {
        try {
            const result = await GameAPI.generateEncounter(detectionLevel);
            const resultDiv = document.getElementById('encounterResult');

            const typeIcons = {
                'DETECTION': '👁️', 'ALLY': '🤝', 'HOSTILE': '⚔️', 'VISION': '👁️‍🗨️',
                'ENTITY': '👻', 'SURGE': '⚡', 'SUPPRESSION': '🔇', 'ARTIFACT': '💎'
            };
            const icon = typeIcons[result.type] || '🌀';

            resultDiv.innerHTML = `
                <div style="padding:10px; background:rgba(167,139,250,0.15); border-radius:6px; border:1px solid rgba(167,139,250,0.3);">
                    <div style="font-weight:600; color:#a78bfa; margin-bottom:4px;">${icon} ${result.typeDisplay}</div>
                    <div style="font-size:0.8rem; color:var(--text-secondary); font-style:italic;">${result.narrative}</div>
                    <div style="font-size:0.7rem; color:var(--text-muted); margin-top:4px;">Roll: ${result.roll}</div>
                </div>
            `;
        } catch (e) {
            showToast('Encounter generation failed: ' + e.message, 'error');
        }
    }

    function init() {
        const closeBtn = document.getElementById('psionicModalClose');
        if (closeBtn) closeBtn.addEventListener('click', hidePsionicModal);

        // Command bar button
        const psionicBtn = document.getElementById('psionicBtn');
        if (psionicBtn) {
            psionicBtn.addEventListener('click', () => {
                // Try to get the current character ID from the game state
                const charId = window.GameUI && window.GameUI.currentCharacterId
                    ? window.GameUI.currentCharacterId
                    : 1; // Default to first character
                showPsionicModal(charId);
            });
        }
    }

    return {
        showPsionicModal,
        hidePsionicModal,
        switchTab,
        detectPsionic,
        restPsionic,
        addDiscipline,
        performPowerCheck,
        generateEncounter,
        init
    };
})();

document.addEventListener('DOMContentLoaded', () => PsionicUI.init());
