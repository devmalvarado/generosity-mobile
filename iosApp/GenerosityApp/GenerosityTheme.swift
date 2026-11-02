import SwiftUI
import UIKit
import Shared

func adaptiveColor(_ light: UInt, _ dark: UInt) -> Color {
    Color(uiColor: UIColor { traits in
        let hex = traits.userInterfaceStyle == .dark ? dark : light
        return UIColor(red: Double((hex >> 16) & 255) / 255, green: Double((hex >> 8) & 255) / 255,
                       blue: Double(hex & 255) / 255, alpha: 1)
    })
}

enum GenTypography {
    static func font(_ style: Font.TextStyle) -> Font {
        let family: String
        let nativeStyle: UIFont.TextStyle
        switch style {
        case .largeTitle: family = GenType.heading; nativeStyle = .largeTitle
        case .title: family = GenType.heading; nativeStyle = .title1
        case .title2: family = GenType.heading; nativeStyle = .title2
        case .title3: family = GenType.heading; nativeStyle = .title3
        case .headline: family = GenType.heading; nativeStyle = .headline
        case .subheadline: family = GenType.body; nativeStyle = .subheadline
        case .callout: family = GenType.body; nativeStyle = .callout
        case .footnote: family = GenType.body; nativeStyle = .footnote
        case .caption: family = GenType.body; nativeStyle = .caption1
        case .caption2: family = GenType.body; nativeStyle = .caption2
        default: family = GenType.body; nativeStyle = .body
        }
        if family == "system" { return .system(style, design: GenType.design) }
        // The base size stays unscaled; SwiftUI applies Dynamic Type exactly once.
        let size = UIFont.preferredFont(forTextStyle: nativeStyle,
            compatibleWith: UITraitCollection(preferredContentSizeCategory: .large)).pointSize
        let font = Font.custom(family, size: size, relativeTo: style)
        return style == .headline ? font.weight(.semibold) : font
    }
}

struct BrandWordmark: View {
    var body: some View {
        Image("GenerosityWordmark")
            .resizable()
            .scaledToFit()
            .frame(width: GenSize.wordmarkWidth, height: GenSize.wordmarkHeight)
            .accessibilityLabel("Generosity")
            .accessibilityIdentifier("brand-wordmark")
    }
}

extension CenterCategory {
    var symbol: String {
        switch self {
        case .bloodDonation: return "drop.fill"
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
        case .bloodDonation, .health, .disabilitySupport: return GenPalette.categoryHealth
        case .communityKitchen, .olderAdults: return GenPalette.categoryCommunity
        default: return GenPalette.categoryOther
        }
    }
}

struct IconTile: View {
    let symbol: String
    var tint: Color = GenPalette.accent
    var size: CGFloat = GenSize.badge
    var body: some View {
        Image(systemName: symbol).font(.system(size: 21, weight: .medium))
            .foregroundStyle(tint).frame(width: size, height: size)
            .background(tint.opacity(0.12), in: RoundedRectangle(cornerRadius: GenRadius.card))
            .accessibilityHidden(true)
    }
}

struct CategoryBadge: View {
    let category: CenterCategory?
    var size: CGFloat = GenSize.badge
    var body: some View { IconTile(symbol: category?.symbol ?? "person.2", tint: category?.tint ?? GenPalette.accent, size: size) }
}
