// Generated from design/tokens.json. Run scripts/generate-design-system.py; do not edit.
import SwiftUI

enum GenPalette {
    static let accent = adaptiveColor(0xC6401D, 0xFFB197)
    static let onAccent = adaptiveColor(0xFFFFFF, 0x4C1500)
    static let accentContainer = adaptiveColor(0xFFEDE5, 0x592717)
    static let onAccentContainer = adaptiveColor(0x8F2C14, 0xFFD9CC)
    static let background = adaptiveColor(0xFAF8F5, 0x17191C)
    static let surface = adaptiveColor(0xFFFFFF, 0x222529)
    static let text = adaptiveColor(0x191C22, 0xF7F5F2)
    static let surfaceVariant = adaptiveColor(0xF2F0ED, 0x30343A)
    static let secondary = adaptiveColor(0x59616C, 0xB8BCC3)
    static let outline = adaptiveColor(0x757B83, 0x9299A3)
    static let line = adaptiveColor(0xE4E2DF, 0x444950)
    static let error = adaptiveColor(0xB3261E, 0xFFB4AB)
    static let onError = adaptiveColor(0xFFFFFF, 0x690005)
    static let categoryHealth = adaptiveColor(0x357D64, 0xA7DAC7)
    static let categoryCommunity = adaptiveColor(0x9A581D, 0xEEC095)
    static let categoryOther = adaptiveColor(0x7060A5, 0xD6C9FF)
}

enum GenSpace {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 12
    static let lg: CGFloat = 16
    static let xl: CGFloat = 20
    static let xxl: CGFloat = 24
    static let welcome: CGFloat = 28
    static let section: CGFloat = 32
}

enum GenRadius {
    static let control: CGFloat = 10
    static let card: CGFloat = 12
    static let panel: CGFloat = 20
    static let feature: CGFloat = 24
    static let map: CGFloat = 28
    static let pill: CGFloat = 999
}

enum GenSize {
    static let wordmarkWidth: CGFloat = 180
    static let wordmarkHeight: CGFloat = 36
    static let symbol: CGFloat = 136
    static let icon: CGFloat = 24
    static let badge: CGFloat = 44
    static let androidTouch: CGFloat = 48
    static let iosTouch: CGFloat = 44
    static let webTouch: CGFloat = 44
}

enum GenType {
    static let body = "system"
    static let heading = "system"
    static let design: Font.Design = .default
}
