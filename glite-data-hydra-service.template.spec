Summary: gLite Data Hydra KeyStore
Name: glite-data-hydra-service
Version: @VERSION@
Release: @RELEASE@@RELEASE.SUFFIX@
License: Apache2
Vendor: gLite
Group: System/Middleware
Prefix: /opt/glite
BuildArch: noarch
BuildRoot: %{_builddir}/%{name}-root
AutoReqProv: yes
Source: %{name}-%{version}.tar.gz

%define debug_package %{nil}

%description
gLite Hydra KeyStore

%prep
rm -rf $RPM_BUILD_ROOT
%setup -q

%install
ant -Dprefix=$RPM_BUILD_ROOT/%{prefix} ${EXTRA_CONFIGURE_OPTIONS} install

%clean
rm -rf $RPM_BUILD_ROOT 

%files
%defattr(-,root,root)
%{prefix}/etc/glite-data-hydra-service/schema/mysql/*
%{prefix}/etc/glite-data-hydra-service/*
%{prefix}/share/java/glite-data-hydra-service.war
%doc %{prefix}/share/doc/glite-data-hydra-service/*

