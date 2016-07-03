Pod::Spec.new do |s|
  s.name             = 'react-native-odnoklassniki-login'
  s.version          = '0.0.1'
  s.summary          = 'A React Native module that wraps Odnoklassniki SDK'
  s.requires_arc = true
  s.homepage         = 'https://github.com/doomsower/react-native-odnoklassniki-login'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'Konstantin Kuznetsov' => 'K.Kuznetcov@gmail.com' }
  s.source           = { :git => 'https://github.com/doomsower/react-native-odnoklassniki-login.git', :tag => s.version.to_s }

  s.source_files = 'ios/*.{h,m}'

  s.platform     = :ios, "7.0"

  s.dependency 'React'
  s.dependency 'ok-ios-sdk'
end