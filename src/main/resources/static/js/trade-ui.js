/* DoraMCU - Trade UI Module (Enhanced with Credit Exchange, Career Bonuses, Route Network, Economic Events) */

const TradeUI = (function() {
    let currentSessionId = null;
    let currentSystemId = null;
    let commodities = [];
    let marketPrices = {};
    let tradeRoutes = [];
    let economicEvents = [];
    let careerSummary = {};
    let exchangeSummary = {};

    function displayMercenary(mercenary) {
        const section = document.getElementById('mercenarySection');
        if (!mercenary || mercenary.totalUnits === 0) {
            section.style.display = 'none';
            return;
        }
        section.style.display = 'block';
        document.getElementById('mercenaryUnitCount').textContent = mercenary.totalUnits || 0;
        document.getElementById('mercenaryGroundStrength').textContent = mercenary.groundStrength || 0;
        document.getElementById('mercenaryTicketStatus').textContent = mercenary.ticketStatus || '—';
        document.getElementById('mercenaryTicketIncome').textContent = mercenary.ticketIncome || 0;
    }

    async function showTradeModal(sessionId, systemId) {
        currentSessionId = sessionId;
        currentSystemId = systemId;
        const modal = document.getElementById('tradeModal');
        if (modal) {
            modal.classList.remove('hidden');
            document.getElementById('tradeMarketInfo').textContent = `Loading market data...`;
            await loadAllTradeData();
        }
    }

    function hideTradeModal() {
        const modal = document.getElementById('tradeModal');
        if (modal) modal.classList.add('hidden');
    }

    async function loadAllTradeData() {
        try {
            // Load commodities list
            commodities = await GameAPI.getCommodities();

            // Load market prices for current system
            try {
                const prices = await GameAPI.getMarketPrices(currentSystemId);
                marketPrices = prices.marketPrices || {};
            } catch (e) {
                marketPrices = {};
            }

            // Load career summary
            try {
                careerSummary = await GameAPI.getTradeCareerSummary(currentSessionId);
            } catch (e) {
                careerSummary = {};
            }

            // Load exchange summary
            try {
                exchangeSummary = await GameAPI.getExchangeSummary(currentSessionId);
            } catch (e) {
                exchangeSummary = {};
            }

            // Load economic events
            try {
                economicEvents = await GameAPI.getEconomicEvents(currentSessionId);
            } catch (e) {
                economicEvents = [];
            }

            // Load trade routes
            try {
                tradeRoutes = await GameAPI.getTradeRoutes(currentSessionId);
            } catch (e) {
                tradeRoutes = [];
            }

            renderAllPanels();
        } catch (e) {
            document.getElementById('tradeMarketInfo').textContent = 'Failed to load market data';
            console.error('Trade market load failed:', e);
        }
    }

    function renderAllPanels() {
        renderMarketInfo();
        renderCommodityList();
        renderTradeRoutes();
        renderCareerBonuses();
        renderExchangeInfo();
        renderEconomicEvents();
        renderMissionPanel();
    }

    function renderMarketInfo() {
        const info = document.getElementById('tradeMarketInfo');
        const credits = exchangeSummary.tradeCredits || 0;
        const bonus = careerSummary.tradeProfitBonus || 0;
        info.innerHTML = `
            <div style="display:flex;gap:16px;flex-wrap:wrap;margin-bottom:8px;">
                <span>🌍 System ${currentSystemId}</span>
                <span>📦 ${commodities.length} commodities</span>
                <span>💰 ${credits.toLocaleString()} credits</span>
                ${bonus > 0 ? `<span>🎖️ +${bonus.toFixed(1)}% trade bonus</span>` : ''}
            </div>
        `;
    }

    function renderCommodityList() {
        const list = document.getElementById('tradeCommodityList');
        if (!list) return;

        if (commodities.length === 0) {
            list.innerHTML = '<div style="text-align:center;color:var(--text-muted);padding:20px;">No trade opportunities</div>';
            return;
        }

        let html = '<div style="display:grid;gap:8px;">';
        commodities.forEach(commodity => {
            const price = marketPrices[commodity] || '—';
            const priceDisplay = typeof price === 'number' ? `${price} cr` : price;
            html += `
                <div style="display:flex;align-items:center;justify-content:space-between;padding:8px 12px;
                    background:rgba(255,255,255,0.03);border-radius:6px;">
                    <div>
                        <div style="font-weight:500;">${commodity}</div>
                        <div style="font-size:0.75rem;color:var(--text-secondary);">Price: ${priceDisplay}</div>
                    </div>
                    <div style="display:flex;gap:4px;">
                        <button class="btn-small" onclick="TradeUI.buyItem('${commodity}')" style="padding:4px 12px;font-size:0.75rem;">Buy</button>
                        <button class="btn-small primary" onclick="TradeUI.sellItem('${commodity}')" style="padding:4px 12px;font-size:0.75rem;">Sell</button>
                    </div>
                </div>
            `;
        });
        html += '</div>';
        list.innerHTML = html;
    }

    function renderTradeRoutes() {
        const container = document.getElementById('tradeRoutesList');
        if (!container) return;

        if (!tradeRoutes || tradeRoutes.length === 0) {
            container.innerHTML = '<div style="text-align:center;color:var(--text-muted);padding:12px;font-size:0.85rem;">No trade routes available</div>';
            return;
        }

        let html = '<div style="display:grid;gap:6px;max-height:200px;overflow-y:auto;">';
        tradeRoutes.slice(0, 10).forEach(route => {
            const profit = route.profitPerUnit || 0;
            const profitClass = profit > 0 ? 'color:#22c55e' : 'color:#ef4444';
            html += `
                <div style="display:flex;align-items:center;justify-content:space-between;padding:6px 10px;
                    background:rgba(255,255,255,0.02);border-radius:4px;font-size:0.8rem;">
                    <div>
                        <div>${route.sourceWorldName || route.sourcePlanetId} → ${route.destWorldName || route.destPlanetId}</div>
                        <div style="font-size:0.7rem;color:var(--text-secondary);">${route.commodity || ''}</div>
                    </div>
                    <div style="${profitClass};font-weight:600;">${profit > 0 ? '+' : ''}${profit} cr</div>
                </div>
            `;
        });
        html += '</div>';
        container.innerHTML = html;
    }

    function renderCareerBonuses() {
        const container = document.getElementById('careerBonuses');
        if (!container) return;

        if (!careerSummary || !careerSummary.hasCommander) {
            container.innerHTML = '<div style="text-align:center;color:var(--text-muted);padding:12px;font-size:0.85rem;">No commander assigned</div>';
            return;
        }

        const bonus = careerSummary.tradeProfitBonus || 0;
        const cargo = careerSummary.cargoCapacityBonus || 0;
        const crew = careerSummary.crewSkillModifier || 1.0;
        const missions = careerSummary.tradeMissionsCompleted || 0;
        const earnings = careerSummary.tradeEarnings || 0;

        container.innerHTML = `
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;">
                <div style="padding:8px;background:rgba(6,182,212,0.1);border-radius:6px;">
                    <div style="font-size:0.7rem;color:var(--text-secondary);">Trade Bonus</div>
                    <div style="font-size:1.1rem;font-weight:600;color:#06b6d4;">+${bonus.toFixed(1)}%</div>
                </div>
                <div style="padding:8px;background:rgba(139,92,246,0.1);border-radius:6px;">
                    <div style="font-size:0.7rem;color:var(--text-secondary);">Cargo Bonus</div>
                    <div style="font-size:1.1rem;font-weight:600;color:#8b5cf6;">+${cargo.toFixed(0)}%</div>
                </div>
                <div style="padding:8px;background:rgba(34,197,94,0.1);border-radius:6px;">
                    <div style="font-size:0.7rem;color:var(--text-secondary);">Crew Skill</div>
                    <div style="font-size:1.1rem;font-weight:600;color:#22c55e;">${crew.toFixed(2)}x</div>
                </div>
                <div style="padding:8px;background:rgba(234,179,8,0.1);border-radius:6px;">
                    <div style="font-size:0.7rem;color:var(--text-secondary);">Missions</div>
                    <div style="font-size:1.1rem;font-weight:600;color:#eab308;">${missions}</div>
                </div>
            </div>
            ${careerSummary.commanderName ? `<div style="margin-top:8px;font-size:0.75rem;color:var(--text-secondary);">Commander: ${careerSummary.commanderName} (${careerSummary.primaryCareer || 'None'})</div>` : ''}
            ${careerSummary.hasMerchantCareer ? '<div style="margin-top:4px;font-size:0.75rem;color:#22c55e;">✓ Merchant Career Active</div>' : ''}
        `;
    }

    function renderExchangeInfo() {
        const container = document.getElementById('exchangeInfo');
        if (!container) return;

        const credits = exchangeSummary.tradeCredits || 0;
        const conversions = exchangeSummary.resourceConversions || {};
        const conversionEntries = Object.entries(conversions);

        container.innerHTML = `
            <div style="padding:8px;background:rgba(234,179,8,0.1);border-radius:6px;margin-bottom:8px;">
                <div style="font-size:0.7rem;color:var(--text-secondary);">Credit Balance</div>
                <div style="font-size:1.3rem;font-weight:700;color:#eab308;">${credits.toLocaleString()} cr</div>
            </div>
            ${conversionEntries.length > 0 ? `
                <div style="font-size:0.75rem;color:var(--text-secondary);margin-bottom:4px;">Resource Conversions:</div>
                <div style="display:flex;flex-wrap:wrap;gap:4px;">
                    ${conversionEntries.map(([key, val]) => `
                        <span style="padding:2px 8px;background:rgba(255,255,255,0.05);border-radius:4px;font-size:0.7rem;">
                            ${key}: ${val}
                        </span>
                    `).join('')}
                </div>
            ` : '<div style="font-size:0.75rem;color:var(--text-muted);">No conversions yet</div>'}
        `;
    }

    function renderEconomicEvents() {
        const container = document.getElementById('economicEvents');
        if (!container) return;

        if (!economicEvents || economicEvents.length === 0) {
            container.innerHTML = '<div style="text-align:center;color:var(--text-muted);padding:12px;font-size:0.85rem;">No active economic events</div>';
            return;
        }

        const eventIcons = {
            'PIRACY': '🏴‍☠️',
            'EMBARGO': '🚫',
            'BOOM': '📈',
            'BUST': '📉',
            'BLOCKADE': '⚓',
            'DIPLOMATIC': '🤝'
        };

        let html = '<div style="display:grid;gap:6px;">';
        economicEvents.forEach(event => {
            const icon = eventIcons[event.type] || '⚡';
            html += `
                <div style="padding:8px 10px;background:rgba(239,68,68,0.1);border-left:3px solid #ef4444;border-radius:4px;">
                    <div style="display:flex;justify-content:space-between;align-items:center;">
                        <span style="font-size:0.85rem;font-weight:500;">${icon} ${event.type}</span>
                        <span style="font-size:0.7rem;color:var(--text-secondary);">${event.turnsRemaining} turns</span>
                    </div>
                    <div style="font-size:0.75rem;color:var(--text-secondary);margin-top:2px;">${event.description || event.world || ''}</div>
                </div>
            `;
        });
        html += '</div>';
        container.innerHTML = html;
    }

    function renderMissionPanel() {
        const select = document.getElementById('tradeMissionCommodity');
        if (!select) return;

        // Only populate if empty
        if (select.options.length > 0) return;

        commodities.forEach(commodity => {
            const opt = document.createElement('option');
            opt.value = commodity;
            opt.textContent = commodity;
            select.appendChild(opt);
        });
    }

    async function buyItem(commodity) {
        const quantity = prompt(`Quantity of ${commodity} to buy:`, '1');
        if (!quantity || parseInt(quantity) <= 0) return;

        try {
            await GameAPI.buyCommodity(currentSessionId, {
                operation: 'BUY',
                worldId: currentSystemId,
                commodity: commodity,
                quantity: parseInt(quantity)
            });
            showToast(`Bought ${quantity} units of ${commodity}`, 'success');
            await loadAllTradeData();
            if (window.GameUI && window.GameUI.refreshGameState) {
                window.GameUI.refreshGameState();
            }
        } catch (e) {
            showToast('Trade failed: ' + e.message, 'error');
        }
    }

    async function sellItem(commodity) {
        const quantity = prompt(`Quantity of ${commodity} to sell:`, '1');
        if (!quantity || parseInt(quantity) <= 0) return;

        try {
            await GameAPI.sellCommodity(currentSessionId, {
                operation: 'SELL',
                worldId: currentSystemId,
                commodity: commodity,
                quantity: parseInt(quantity)
            });
            showToast(`Sold ${quantity} units of ${commodity}`, 'success');
            await loadAllTradeData();
            if (window.GameUI && window.GameUI.refreshGameState) {
                window.GameUI.refreshGameState();
            }
        } catch (e) {
            showToast('Trade failed: ' + e.message, 'error');
        }
    }

    async function executeTradeMission() {
        const destInput = document.getElementById('tradeMissionDest');
        const commoditySelect = document.getElementById('tradeMissionCommodity');
        const qtyInput = document.getElementById('tradeMissionQty');
        const resultDiv = document.getElementById('tradeMissionResult');

        if (!destInput || !commoditySelect || !qtyInput) {
            // Fallback to prompt if form elements not found
            const destSystemId = prompt('Destination system ID:', '');
            if (!destSystemId) return;
            try {
                const result = await GameAPI.executeTradeMission(currentSessionId, {
                    operation: 'TRADE_MISSION',
                    worldId: currentSystemId,
                    destinationWorldId: parseInt(destSystemId),
                    shipDesignId: 1
                });
                if (result && result.profit !== undefined) {
                    const profitStr = result.profit >= 0 ? `+${result.profit}` : result.profit;
                    showToast(`Trade mission complete: ${profitStr} credits (${result.roi?.toFixed(1) || 0}% ROI)`, result.profit >= 0 ? 'success' : 'error');
                    await loadAllTradeData();
                } else {
                    showToast(result?.message || 'Trade mission failed', 'error');
                }
            } catch (e) {
                showToast('Trade mission failed: ' + e.message, 'error');
            }
            return;
        }

        const destWorldId = parseInt(destInput.value);
        const commodity = commoditySelect.value;
        const quantity = parseInt(qtyInput.value);

        if (!destWorldId || !commodity || !quantity || quantity <= 0) {
            showToast('Please fill in all fields correctly', 'error');
            return;
        }

        resultDiv.style.display = 'block';
        resultDiv.innerHTML = '<div style="color:var(--text-secondary);">⏳ Executing trade mission...</div>';

        try {
            // First buy the commodity at source
            const buyResult = await GameAPI.buyCommodity(currentSessionId, {
                operation: 'BUY',
                worldId: currentSystemId,
                commodity: commodity,
                quantity: quantity
            });

            // Then sell at destination
            const sellResult = await GameAPI.sellCommodity(currentSessionId, {
                operation: 'SELL',
                worldId: destWorldId,
                commodity: commodity,
                quantity: quantity
            });

            const cost = buyResult?.cost || 0;
            const revenue = sellResult?.revenue || 0;
            const profit = revenue - cost;
            const roi = cost > 0 ? (profit / cost * 100) : 0;

            const profitClass = profit >= 0 ? 'color:#22c55e' : 'color:#ef4444';
            resultDiv.innerHTML = `
                <div style="font-weight:600; margin-bottom:6px;">📊 Trade Mission Results</div>
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:6px; font-size:0.85rem;">
                    <div>Source: System ${currentSystemId}</div>
                    <div>Destination: System ${destWorldId}</div>
                    <div>Commodity: ${commodity}</div>
                    <div>Quantity: ${quantity}</div>
                    <div>Cost: <span style="color:#ef4444;">${cost.toLocaleString()} cr</span></div>
                    <div>Revenue: <span style="color:#22c55e;">${revenue.toLocaleString()} cr</span></div>
                    <div style="grid-column:span 2; text-align:center; padding:6px; background:rgba(255,255,255,0.05); border-radius:4px;">
                        Profit: <span style="${profitClass}; font-weight:700; font-size:1.1rem;">${profit >= 0 ? '+' : ''}${profit.toLocaleString()} cr</span>
                        <span style="${profitClass}; margin-left:8px;">(${roi.toFixed(1)}% ROI)</span>
                    </div>
                </div>
            `;

            showToast(`Trade mission: ${profit >= 0 ? '+' : ''}${profit.toLocaleString()} cr profit`, profit >= 0 ? 'success' : 'error');
            await loadAllTradeData();
        } catch (e) {
            resultDiv.innerHTML = `<div style="color:#ef4444;">❌ Trade failed: ${e.message || 'Unknown error'}</div>`;
            showToast('Trade mission failed: ' + (e.message || 'Unknown error'), 'error');
        }
    }

    function switchTab(tabName) {
        // Update tab buttons
        document.querySelectorAll('.trade-tab').forEach(tab => {
            if (tab.dataset.tab === tabName) {
                tab.classList.add('active');
                tab.style.background = 'rgba(6,182,212,0.2)';
                tab.style.color = '#06b6d4';
            } else {
                tab.classList.remove('active');
                tab.style.background = 'rgba(255,255,255,0.05)';
                tab.style.color = 'var(--text-secondary)';
            }
        });
        // Update panels
        document.querySelectorAll('.trade-panel').forEach(panel => panel.classList.add('hidden'));
        const panelMap = {
            'commodities': 'commoditiesPanel',
            'routes': 'routesPanel',
            'career': 'careerPanel',
            'exchange': 'exchangePanel',
            'events': 'eventsPanel',
            'mission': 'missionPanel'
        };
        const panel = document.getElementById(panelMap[tabName]);
        if (panel) panel.classList.remove('hidden');
    }

    function init() {
        document.getElementById('tradeModalClose').addEventListener('click', hideTradeModal);
        const missionBtn = document.getElementById('tradeMissionBtn');
        if (missionBtn) {
            missionBtn.addEventListener('click', executeTradeMission);
        }
    }

    return {
        displayMercenary,
        showTradeModal,
        hideTradeModal,
        buyItem,
        sellItem,
        executeTradeMission,
        switchTab,
        init
    };
})();

document.addEventListener('DOMContentLoaded', () => TradeUI.init());
