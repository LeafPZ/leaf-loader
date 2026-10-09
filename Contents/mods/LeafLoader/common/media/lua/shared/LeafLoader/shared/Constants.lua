local Calendar = Calendar
local Events = Events

local getModInfoByID = getModInfoByID

---@class (exact) Constants
---@field CALENDAR_MONTH  integer
---@field MOD_ID          string
local Constants = {}

Constants.CALENDAR_MONTH = Calendar.getInstance():get(Calendar.MONTH)
Constants.MOD_ID = "LeafLoader"

return Constants
