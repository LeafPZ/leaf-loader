local Constants = require("LeafLoader/shared/Constants")

local getText = getText

local Utils = {}

--- @param category string
--- @param s string
--- @return string
function Utils.getText(category, s)
    return getText(category .. "_" .. Constants.MOD_ID .. "_" .. s)
end

return Utils
