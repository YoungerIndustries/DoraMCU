/* DoraMCU - Character UI Module */

const CharacterUI = (function() {
    let currentCharacter = null;

    function displayCommander(commander) {
        const section = document.getElementById('commanderSection');
        if (!commander || !commander.name) {
            section.style.display = 'none';
            return;
        }
        section.style.display = 'block';
        document.getElementById('commanderName').textContent = commander.name;
        document.getElementById('commanderCareer').textContent = commander.career || '—';

        // Display abilities
        if (commander.abilities) {
            const abilityMap = { ST: 'abilityST', DX: 'abilityDX', EN: 'abilityEN', IN: 'abilityIN', ED: 'abilityED', SO: 'abilitySO' };
            for (const [key, id] of Object.entries(abilityMap)) {
                const el = document.getElementById(id);
                if (el) el.textContent = commander.abilities[key] || '—';
            }
        }

        // Display skills
        const skillsList = document.getElementById('skillsList');
        if (skillsList && commander.skills && commander.skills.length > 0) {
            skillsList.innerHTML = '<div style="font-size:0.75rem;color:var(--text-secondary);margin-bottom:4px;">Skills</div>' +
                commander.skills.map(s => `<span class="bonus-badge" style="margin:2px;display:inline-block;padding:2px 8px;font-size:0.7rem;">${s}</span>`).join('');
        } else if (skillsList) {
            skillsList.innerHTML = '';
        }
    }

    function showCharacterModal() {
        const modal = document.getElementById('characterModal');
        if (modal) {
            modal.classList.remove('hidden');
            if (currentCharacter) {
                updateCharacterPreview(currentCharacter);
            }
        }
    }

    function hideCharacterModal() {
        const modal = document.getElementById('characterModal');
        if (modal) modal.classList.add('hidden');
    }

    function updateCharacterPreview(character) {
        currentCharacter = character;
        const preview = document.getElementById('characterPreview');
        if (!preview) return;

        let html = '<div style="padding:12px;background:rgba(255,255,255,0.05);border-radius:8px;">';
        html += `<div style="font-weight:600;margin-bottom:8px;">${character.name || 'Unnamed'}</div>`;
        if (character.abilities) {
            html += '<div style="display:grid;grid-template-columns:repeat(6,1fr);gap:4px;text-align:center;">';
            for (const [key, val] of Object.entries(character.abilities)) {
                html += `<div><div style="font-size:0.7rem;color:var(--text-secondary);">${key}</div><div style="font-weight:600;">${val}</div></div>`;
            }
            html += '</div>';
        }
        if (character.skills && character.skills.length > 0) {
            html += '<div style="margin-top:8px;font-size:0.8rem;">Skills: ' + character.skills.join(', ') + '</div>';
        }
        html += '</div>';
        preview.innerHTML = html;
    }

    async function generateCharacter() {
        const name = document.getElementById('characterNameInput').value.trim() || 'Adventurer';
        try {
            const character = await GameAPI.generateCharacter(name);
            updateCharacterPreview(character);
            showToast('Character generated!', 'success');
        } catch (e) {
            showToast('Failed to generate character: ' + e.message, 'error');
        }
    }

    async function serveCareer() {
        const career = document.getElementById('careerSelect').value;
        if (!currentCharacter || !currentCharacter.id) {
            showToast('Generate a character first!', 'warning');
            return;
        }
        try {
            const tour = await GameAPI.serveCareer(currentCharacter.id, career);
            // Update character with new data
            const updated = await GameAPI.getCharacter(currentCharacter.id);
            updateCharacterPreview(updated);
            showToast('Career term completed!', 'success');
            // Refresh game state to update commander display
            if (window.GameUI && window.GameUI.refreshGameState) {
                window.GameUI.refreshGameState();
            }
        } catch (e) {
            showToast('Failed to serve career: ' + e.message, 'error');
        }
    }

    function init() {
        document.getElementById('characterModalClose').addEventListener('click', hideCharacterModal);
        document.getElementById('characterGenerateBtn').addEventListener('click', generateCharacter);
        document.getElementById('characterServeBtn').addEventListener('click', serveCareer);
        document.getElementById('developCharacterBtn').addEventListener('click', showCharacterModal);
    }

    return {
        displayCommander,
        showCharacterModal,
        hideCharacterModal,
        init
    };
})();

document.addEventListener('DOMContentLoaded', () => CharacterUI.init());
