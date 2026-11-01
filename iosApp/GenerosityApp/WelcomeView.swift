import SwiftUI
import SharedBridge

struct WelcomeView: View {
    let locale: AppLocale
    @ObservedObject var account: AccountSession
    let store: GenerosityStore
    let onGuest: () -> Void
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                Spacer(minLength: 48)
                Image("GenerositySymbol").resizable().scaledToFit().frame(width: 136, height: 136).accessibilityHidden(true)
                BrandWordmark()
                Text(L10n.resource("welcome_tagline", locale: locale)).font(.title2).multilineTextAlignment(.center)
                Spacer(minLength: 32)
                Button(action: onGuest) { Text(L10n.resource("welcome_guest", locale: locale)).foregroundStyle(GenPalette.onAccent).frame(maxWidth: .infinity, minHeight: 44) }.buttonStyle(.borderedProminent)
                Button { account.signIn(store: store) } label: { Text(L10n.resource("auth_sign_in", locale: locale)).frame(maxWidth: .infinity, minHeight: 44) }.buttonStyle(.bordered).disabled(account.busy)
                Button { account.signIn(store: store, register: true) } label: { Text(L10n.resource("welcome_create", locale: locale)).frame(maxWidth: .infinity, minHeight: 44) }.disabled(account.busy)
                if account.busy { ProgressView() }
                if account.failed { Text(L10n.resource("auth_failed", locale: locale)).foregroundStyle(GenPalette.error) }
            }.padding(28)
        }.frame(maxWidth: .infinity, maxHeight: .infinity).background(GenPalette.background).foregroundStyle(GenPalette.text)
    }
}
