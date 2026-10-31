import SwiftUI
import UIKit
import Shared

enum GenPalette {
    static func adaptive(_ light: UInt, _ dark: UInt) -> Color {
        Color(uiColor: UIColor { traits in
            let hex = traits.userInterfaceStyle == .dark ? dark : light
            return UIColor(red: Double((hex >> 16) & 255) / 255, green: Double((hex >> 8) & 255) / 255,
                           blue: Double(hex & 255) / 255, alpha: 1)
        })
    }
    static let accent = adaptive(0xC6401D, 0xFFB197)
    static let onAccent = adaptive(0xFFFFFF, 0x4C1500)
    static let background = adaptive(0xFAF8F5, 0x17191C)
    static let surface = adaptive(0xFFFFFF, 0x222529)
    static let text = adaptive(0x191C22, 0xF7F5F2)
    static let secondary = adaptive(0x59616C, 0xB8BCC3)
    static let line = adaptive(0xE4E2DF, 0x444950)
    static let error = adaptive(0xB3261E, 0xFFB4AB)
}

struct BrandWordmark: View {
    var body: some View {
        Image("GenerosityWordmark")
            .resizable()
            .scaledToFit()
            .frame(width: 180, height: 36)
            .accessibilityLabel("Generosity")
            .accessibilityIdentifier("brand-wordmark")
    }
}

extension CenterCategory {
    var symbol: String {
        switch self {
        case .health, .organTissueDonation: return "cross.case"
        case .education: return "graduationcap"
        case .communityKitchen: return "fork.knife"
        case .addictionRecovery: return "heart"
        case .disabilitySupport: return "figure.roll"
        default: return "person.2"
        }
    }
    var tint: Color {
        switch self {
        case .health, .disabilitySupport: return GenPalette.adaptive(0x357D64, 0xA7DAC7)
        case .communityKitchen, .olderAdults: return GenPalette.adaptive(0x9A581D, 0xEEC095)
        default: return GenPalette.adaptive(0x7060A5, 0xD6C9FF)
        }
    }
}

struct IconTile: View {
    let symbol: String
    var tint: Color = GenPalette.accent
    var size: CGFloat = 44
    var body: some View {
        Image(systemName: symbol).font(.system(size: 21, weight: .medium))
            .foregroundStyle(tint).frame(width: size, height: size)
            .background(tint.opacity(0.12), in: RoundedRectangle(cornerRadius: 12))
            .accessibilityHidden(true)
    }
}

struct CategoryBadge: View {
    let category: CenterCategory?
    var size: CGFloat = 44
    var body: some View { IconTile(symbol: category?.symbol ?? "person.2", tint: category?.tint ?? GenPalette.accent, size: size) }
}
