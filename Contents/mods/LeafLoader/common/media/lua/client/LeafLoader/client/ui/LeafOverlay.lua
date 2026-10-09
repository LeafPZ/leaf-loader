---@class LeafOverlay: ISPanel
---@field instance           LeafOverlay
---@field leafIconTex        Texture
---@field leafIcon           ISImage
---@field padding            number
---@field init               function
---@field onResolutionChange function

local Constants = require("LeafLoader/shared/Constants")
local Utils = require("LeafLoader/shared/Utils")

local Events = Events
local ISImage = ISImage
local ISPanel = ISPanel

local assert = assert
local getCore = getCore
local getTexture = getTexture

---@type LeafOverlay
local LeafOverlay = ISPanel:derive(Constants.MOD_ID .. "_LeafOverlay")

---@param inGame boolean
function LeafOverlay.init(inGame)
    local ui = LeafOverlay:new(0, 0, 0, 0)
    ui:initialise()
    ui:setAlwaysOnTop(false)
    ui:setVisible(true)
    ui:setEnabled(true)
    ui:addToUIManager()
    ui:bringToTop()
end

function LeafOverlay:new()
    if self.instance then
        return self.instance
    end

    local leafIconTex = nil

    if Constants.CALENDAR_MONTH == 6 then
        leafIconTex = getTexture("media/ui/icon-pride.png")
    elseif Constants.CALENDAR_MONTH >= 9 and Constants.CALENDAR_MONTH <= 11 then
        leafIconTex = getTexture("media/ui/icon-autumn.png")
    else
        leafIconTex = getTexture("media/ui/icon.png")
    end

    assert(leafIconTex ~= nil)

    local width = leafIconTex:getWidth() / 2
    local height = width

    ---@type LeafOverlay
    local o = ISPanel.new(self, 0, 0, width, width)
    o:noBackground()

    o.padding = 20
    o.leafIconTex = leafIconTex

    local screenWidth = getCore():getScreenWidth()
    o:setX(screenWidth - width - o.padding)
    o:setY(o.padding)

    self.instance = o
    return self.instance
end

function LeafOverlay:initialise()
    local width = self:getWidth()

    self.leafIcon = ISImage:new(0, 0, width, width, self.leafIconTex)
    self.leafIcon.autoScale = true
    self.leafIcon:setMouseOverText(Utils.getText("UI", "LeafOverlay_Tooltip"))
    self:addChild(self.leafIcon)

    ISPanel.initialise(self)
end

function LeafOverlay:onResolutionChange(oldW, oldH, newW, newH)
    self:setX(newW - (self:getWidth()) - self.padding)
    self:setY(self.padding)
end

function LeafOverlay:prerender()
    ISPanel.prerender(self)
end

local function onMainMenuEnter()
    LeafOverlay.init(false)
end

local function onGameStart()
    LeafOverlay.init(true)
end

---@param oldW integer
---@param oldH integer
---@param newW integer
---@param newH integer
local function onResolutionChange(oldW, oldH, newW, newH)
    if not LeafOverlay.instance then
        return
    end

    LeafOverlay.instance:onResolutionChange(oldW, oldH, newW, newH)
end

Events.OnMainMenuEnter.Add(onMainMenuEnter)
Events.OnGameStart.Add(onGameStart)
Events.OnResolutionChange.Add(onResolutionChange)

return LeafOverlay
