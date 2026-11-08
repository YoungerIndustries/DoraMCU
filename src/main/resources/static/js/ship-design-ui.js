/* DoraMCU - Ship Design UI Module */

const ShipDesignUI = (function() {
    let currentSessionId = null;

    function showShipDesignModal(sessionId) {
        currentSessionId = sessionId;
        const modal = document.getElementById('shipDesignModal');
        if (modal) {
            modal.classList.remove('hidden');
            updateSummary();
        }
    }

    function hideShipDesignModal() {
        const modal = document.getElementById('shipDesignModal');
        if (modal) modal.classList.add('hidden');
    }

    function updateSummary() {
        const tonnage = parseInt(document.getElementById('shipDesignTonnage').value) || 2000;
        document.getElementById('tonnageValue').textContent = tonnage;

        const summary = document.getElementById('shipDesignSummary');
        if (!summary) return;

        const jump = parseInt(document.getElementById('shipDesignJump').value) || 0;
        const maneuver = parseInt(document.getElementById('shipDesignManeuver').value) || 0;
        const cargo = parseInt(document.getElementById('shipDesignCargo').value) || 0;
        const fuel = parseInt(document.getElementById('shipDesignFuel').value) || 0;

        // Rough cost estimate
        const cost = Math.round(tonnage * (1 + jump * 0.5 + maneuver * 0.3));
        summary.innerHTML = `
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;font-size:0.85rem;">
                <div><span style="color:var(--text-secondary);">Tonnage:</span> ${tonnage.toLocaleString()}t</div>
                <div><span style="color:var(--text-secondary);">Jump:</span> J-${jump}</div>
                <div><span style="color:var(--text-secondary);">Maneuver:</span> M-${maneuver}</div>
                <div><span style="color:var(--text-secondary);">Cargo:</span> ${cargo}t</div>
                <div><span style="color:var(--text-secondary);">Fuel:</span> ${fuel}t</div>
                <div><span style="color:var(--text-secondary);">Est. Cost:</span> ${cost.toLocaleString()} MC</div>
            </div>
        `;
    }

    async function buildShip() {
        const name = document.getElementById('shipDesignName').value.trim();
        if (!name) {
            showToast('Please enter a ship name', 'warning');
            return;
        }

        const design = {
            name: name,
            totalTonnage: parseInt(document.getElementById('shipDesignTonnage').value),
            jumpRating: parseInt(document.getElementById('shipDesignJump').value),
            maneuverRating: parseInt(document.getElementById('shipDesignManeuver').value),
            cargoTons: parseInt(document.getElementById('shipDesignCargo').value),
            fuelTons: parseInt(document.getElementById('shipDesignFuel').value),
            passengerCount: parseInt(document.getElementById('shipDesignPassengers').value) || 0
        };

        try {
            await GameAPI.buildFromDesign(currentSessionId, design);
            showToast(`Ship "${name}" built successfully!`, 'success');
            hideShipDesignModal();
            // Refresh game state
            if (window.GameUI && window.GameUI.refreshGameState) {
                window.GameUI.refreshGameState();
            }
        } catch (e) {
            showToast('Failed to build ship: ' + e.message, 'error');
        }
    }

    function init() {
        document.getElementById('shipDesignModalClose').addEventListener('click', hideShipDesignModal);
        document.getElementById('shipDesignBuildBtn').addEventListener('click', buildShip);
        // Update summary on input change
        ['shipDesignTonnage', 'shipDesignJump', 'shipDesignManeuver', 'shipDesignCargo', 'shipDesignFuel'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.addEventListener('input', updateSummary);
        });
    }

    return {
        showShipDesignModal,
        hideShipDesignModal,
        init
    };
})();

document.addEventListener('DOMContentLoaded', () => ShipDesignUI.init());
